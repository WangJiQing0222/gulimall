package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.entity.PaymentInfoEntity;
import com.atguigu.gulimall.order.enume.OrderStatusEnum;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WmsFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.service.PaymentInfoService;
import com.atguigu.gulimall.order.to.OrderCreateTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sun.xml.internal.bind.v2.TODO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    WmsFeignService wmsFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    PaymentInfoService paymentInfoService;

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);

            //1、远程查询所有的收货地址列表
            List<MemberAddressVo> address = memberFeignService.getAddress(memberRespVo.getId());
            confirmVo.setAddress(address);
        }, executor);

        //异步执行远程调用 TODO远程调用购物车为，购物车服务有拦截器，认为你没登陆直接返回数据null
        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);

            //2、远程查询购物车中所有选中的内容
            List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
            System.out.println("购物车内容：" + items);
            confirmVo.setItems(items);
        }, executor).thenRunAsync(() -> {
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> collect = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());

            //TODO 一定要启动库存服务
            R hasStock = wmsFeignService.getSkuHasStock(collect);
            List<SkuStockVo> data = hasStock.getData(new TypeReference<List<SkuStockVo>>() {
            });

            if (data != null) {
                System.out.println("远程调用库存不为null----------->:" + data);
                Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                confirmVo.setStocks(map);
            }else {
                System.out.println("远程调用库存为null----------->:" + data);
            }


        },executor);


        //3、查询用户积分
        Integer integration = memberRespVo.getIntegration();
        confirmVo.setIntegration(integration);

        //4、其它数据自动计算

        //TODO 5、防重令牌(防止表单重复提交) 接口幂等性
        //为用户设置一个token，三十分钟过期时间（存在redis）
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(), token, 30, TimeUnit.MINUTES);
        confirmVo.setOrderToken(token);

        //等待远程调用都执行完毕
        CompletableFuture.allOf(cartFuture, getAddressFuture).get();

        return confirmVo;
    }


    /**
     * 提交订单
     * 本地事务：在分布式系统中，只能控制住自己的回滚，控制不了其它服务的回滚
     * 分布式事务：最大原因 网络问题 + 分布式机器
     * @param vo
     * @return
     */
//    @GlobalTransactional
//    @Transactional(rollbackFor = {NoStockException.class})
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        confirmVoThreadLocal.set(vo);
        SubmitOrderResponseVo response = new SubmitOrderResponseVo();

        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        //1、验证令牌是否合法【令牌的对比和删除必须保证原子性】
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();
        System.out.println("防重令牌：" + orderToken);
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()),
                orderToken);

        if(result == 0L){
            //令牌验证失败
            response.setCode(1);
            return response;
        }else {
            //令牌验证成功
            //下单：去创建订单，验令牌，验价格，锁库存.··
            //1、创建订单、订单项
            OrderCreateTo order = createOrder();
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            System.out.println("payAmount:" + payAmount + "payPrice:" + payPrice);
            //2、验价
            if(Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01){
                //金额对比
                //....
                //3、保存订单数据
                saveOrder(order);
                //4、库存锁定。只要有异常回滚订单数据。
                //订单号，所有订单项（skuId,skuName,num)
                WareSkuLockVo lockVo = new WareSkuLockVo();
                List<OrderItemVo> locks = order.getItems().stream().map(item -> {
                    OrderItemVo itemVo = new OrderItemVo();

                    itemVo.setSkuId(item.getSkuId());
                    itemVo.setCount(item.getSkuQuantity());
                    itemVo.setTitle(item.getSkuName());
                    return itemVo;
                }).collect(Collectors.toList());

                lockVo.setOrderSn(order.getOrder().getOrderSn());
                lockVo.setLocks(locks);

                //TODO 远程锁库存
                //库存成功了，但是网络原因超时了，订单回滚，库存不滚
                R r = wmsFeignService.orderLockStock(lockVo);
                if(r.getCode() == 0){
                    //锁成功了
                    response.setOrder(order.getOrder());

                    //TODO 远程扣减积分 出异常
//                    int i = 10 / 0;//订单回滚，库存不滚

                    //TODO 订单创建成功，发送消息给MQ
                    rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",order.getOrder());


                    return response;
                }else {
                    //锁失败了
//                    throw new NoStockException(Long.valueOf("msg"));
                    response.setCode(3);
                    return response;
                }

            }else {
                response.setCode(2);
                return response;
            }

        }
//        String redisToken = redisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId());
//        if(orderToken != null && orderToken.equals(redisToken)){
//            //令牌验证通过
//            redisTemplate.delete(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId());
//        }else {
//            //不通过
//        }

    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        OrderEntity entity = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return entity;
    }

    @Override
    public void closeOrder(OrderEntity entity) {
        //查询当前这个订单的最新状态
        OrderEntity orderEntity = this.getById(entity.getId());
        if (orderEntity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()) {
            //关单
            OrderEntity orderUpdate = new OrderEntity();
            orderUpdate.setId(orderEntity.getId());
            orderUpdate.setStatus(OrderStatusEnum.CANCLED.getCode());

            this.updateById(orderUpdate);

            //TODO 确保每个消息发送成功，给每个消息做好日志记录，(给数据库保存每一个详细信息)保存每个消息的详细信息
            rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderEntity);

        }
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity order = this.getOrderByOrderSn(orderSn);

        BigDecimal bigDecimal = order.getPayAmount().setScale(2, BigDecimal.ROUND_UP);
        payVo.setTotal_amount(bigDecimal.toString());
        payVo.setOut_trade_no(orderSn);

        List<OrderItemEntity> order_sn = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        OrderItemEntity entity = order_sn.get(0);//只要一个就行
        payVo.setSubject(entity.getSkuName());
        payVo.setBody(entity.getSkuAttrsVals());

        return payVo;
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberRespVo respVo = LoginUserInterceptor.loginUser.get();

        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id", respVo.getId()).orderByDesc("id")
        );

        List<OrderEntity> order_sn = page.getRecords().stream().map(order -> {
            List<OrderItemEntity> itemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
            order.setItemEntities(itemEntities);
            return order;
        }).collect(Collectors.toList());

        page.setRecords(order_sn);

        return new PageUtils(page);
    }

    /**
     * 处理支付宝的支付结果
     * @param vo
     * @return
     */
    @Override
    public String handlePayResult(PayAsyncVo vo) {
        //1、保存交易流水
        PaymentInfoEntity infoEntity = new PaymentInfoEntity();
        infoEntity.setAlipayTradeNo(vo.getTrade_no());
        infoEntity.setOrderSn(vo.getOut_trade_no());
        infoEntity.setPaymentStatus(vo.getTrade_status());
        infoEntity.setCallbackTime(vo.getNotify_time());

        paymentInfoService.save(infoEntity);

        //2、修改订单的状态信息
        if(vo.getTrade_status().equals("TRADE_SUCCESS") || vo.getTrade_status().equals("TRADE_FINISHED")){
            //支付成功状态
            String outTradeNo = vo.getOut_trade_no();
            this.baseMapper.updateOrderStatus(outTradeNo, OrderStatusEnum.PAYED.getCode());
        }

        return null;
    }

    @Override
    public void createSeckillOrder(SeckillOrderTo seckillOrderTo) {
        //TODO 保存订单信息
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(seckillOrderTo.getOrderSn());
        orderEntity.setMemberId(seckillOrderTo.getMemberId());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());

        BigDecimal multiply = seckillOrderTo.getSeckillPrice().multiply(new BigDecimal(seckillOrderTo.getNum()));
        orderEntity.setPayAmount(multiply);
        orderEntity.setCreateTime(new Date());
        this.save(orderEntity);

        //TODO 保存订单项信息
        OrderItemEntity itemEntity = new OrderItemEntity();
        itemEntity.setOrderSn(seckillOrderTo.getOrderSn());
        itemEntity.setRealAmount(multiply);
        itemEntity.setSkuQuantity(seckillOrderTo.getNum());
        orderItemService.save(itemEntity);


    }

    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        List<OrderItemEntity> orderItems = order.getItems();

        orderEntity.setModifyTime(new Date());//设置修改时间

        this.save(orderEntity);
        orderItemService.saveBatch(orderItems);
    }


    private OrderCreateTo createOrder(){
        OrderCreateTo createTo = new OrderCreateTo();
        //1、生成订单号
        String orderSn = IdWorker.getTimeId();
        //创建订单号
        OrderEntity orderEntity = buildOrder(orderSn);


        //2、获取到所有的订单项
        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);

        //3、验价
        computePrice(orderEntity, itemEntities);
        createTo.setOrder(orderEntity);
        createTo.setItems(itemEntities);

        return createTo;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {
        BigDecimal total = new BigDecimal("0");

        BigDecimal coupon = new BigDecimal("0");
        BigDecimal integration = new BigDecimal("0");
        BigDecimal promotion = new BigDecimal("0");

        BigDecimal gift = new BigDecimal("0");
        BigDecimal growth = new BigDecimal("0");
        //订单的总额，叠加每一个订单项的总额信息
        for (OrderItemEntity item : itemEntities) {
            total.add(item.getRealAmount());

            coupon.add(item.getCouponAmount());
            integration.add(item.getIntegrationAmount());
            promotion.add(item.getPromotionAmount());

            gift.add(new BigDecimal(item.getGiftIntegration().toString()));
            growth.add(new BigDecimal(item.getGiftGrowth().toString()));
        }

        //1、订单价格相关
        orderEntity.setTotalAmount(total);
        //应付总额 加上运费
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setCouponAmount(coupon);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setPromotionAmount(promotion);

        //设置积分等信息
        orderEntity.setIntegration(gift.intValue());
        orderEntity.setGrowth(growth.intValue());
        orderEntity.setDeleteStatus(0);//未删除


    }

    /**
     * 创建订单号
     * @param orderSn
     * @return
     */
    private OrderEntity buildOrder(String orderSn) {

        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);//设置订单号

        MemberRespVo respVo = LoginUserInterceptor.loginUser.get();
        entity.setMemberId(respVo.getId());//设置会员id

        OrderSubmitVo submitVo = confirmVoThreadLocal.get();

        //获取收货地址信息
        R fare = wmsFeignService.getFare(submitVo.getAddrId());
        FareVo fareVo = fare.getData(new TypeReference<FareVo>() {
        });
        //设置运费
        entity.setFreightAmount(fareVo.getFare());
        //设置收货人信息
        entity.setReceiverCity(fareVo.getAddress().getCity());
        entity.setReceiverDetailAddress(fareVo.getAddress().getDetailAddress());
        entity.setReceiverName(fareVo.getAddress().getName());
        entity.setReceiverPhone(fareVo.getAddress().getPhone());
        entity.setReceiverPostCode(fareVo.getAddress().getPostCode());
        entity.setReceiverProvince(fareVo.getAddress().getProvince());
        entity.setReceiverRegion(fareVo.getAddress().getRegion());

        //设置订单的相关状态信息
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setAutoConfirmDay(7);

        return entity;
    }

    /**
     * 构建所有订单项
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        //最后确定每个购物项的价格
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (currentUserCartItems != null && currentUserCartItems.size() > 0) {
            List<OrderItemEntity> itemEntities = currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity itemEntity = buildOrderItem(cartItem);

                itemEntity.setOrderSn(orderSn);
                return itemEntity;
            }).collect(Collectors.toList());
            return itemEntities;
        }
        return null;
    }

    /**
     * 构建某一个订单项
     * @param cartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity itemEntity = new OrderItemEntity();

        //1、订单信息：订单号 √

        //2、商品的SPU信息
        Long skuId = cartItem.getSkuId();
        R r = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo data = r.getData(new TypeReference<SpuInfoVo>() {
        });
        itemEntity.setSpuId(data.getId());
        itemEntity.setSpuBrand(data.getBrandId().toString());
        itemEntity.setSpuName(data.getSpuName());
        itemEntity.setCategoryId(data.getCatalogId());

        //3、商品的SKU信息 √
        itemEntity.setSkuId(cartItem.getSkuId());
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());
        itemEntity.setSkuPrice(cartItem.getPrice());
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttrValues(), ";");
        itemEntity.setSkuAttrsVals(skuAttr);
        itemEntity.setSkuQuantity(cartItem.getCount());
        //4、优惠信息【不做】
        //5、积分信息 积分 * 购物车商品数量
        itemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());
        itemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());
        //6、订单项的价格信息
        itemEntity.setPromotionAmount(new BigDecimal("0"));//促销价格
        itemEntity.setCouponAmount(new BigDecimal("0"));//优惠卷
        itemEntity.setIntegrationAmount(new BigDecimal("0"));//积分
        //当前订单项的实际金额 总额-各种优惠
        BigDecimal orign = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));
        BigDecimal subtract = orign.subtract(itemEntity.getPromotionAmount())
                .subtract(itemEntity.getCouponAmount())
                .subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(itemEntity.getSkuPrice().multiply(orign));//最后减去所有优惠的真实价格
        


        return itemEntity;
    }
}