package com.atguigu.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.atguigu.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private String app_id = "2021000122615879";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private String merchant_private_key = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCwIZpUHx1psDqkZvFR5DhQMnLif2gJ13ximv3bIw2q2awo/NrCN1Bpznd85kkolc6+4PY2pCnhEwd3NlEJrI2vRdqvEQ6qFs9dhhXo8x8BP5eXCcnMeZbRIVz1rJMGVrM+zarBBf/yFPTyv69lBcoSOWdaULuDewtf8bbOSBa0QkwP6U3I0BUwr6Uk+bSGIyZkQVQc0wC2xmHaP85+eMvbGvba/QpxOuDktAJCDwvSQQZ41tUXgwCvppixo2jotip+xex6qceujexo3jNGRe/6HGG4xyaRZskiEzc7A9L0WWpe3w9fg83wExy51Zfu/hLIm/g6dMsZkRwy+7bjItmRAgMBAAECggEAHdmkESuwzW+rkJ9NuMZH8PeL1u0m0bwufXutGE2JjHIVpmCsJow/WZ8/SvCwZ2t0PY9cywfpfRa/himzdENFQ/Y/yLnIVXVGMaB+6BsEHy8K3vMuuzPEJq9T1OMpi8J34GX00X/Vz85TcSyhaiqgA5Ss+naCQRdCZ0FM7Qr0phnqFuYhPpaJ6EvKNcynquErJ8oVy9JBvbfUhKPJ3awt1JSwSY0zwIiXI9nwccz2WRDkaB5RaSg2DAHv74z38/JDVVHhi5rwG0h+eLN/oJFoCha3vT3eh54sPdYXqgm1r5wBeTgP81IT1fUKrX/mdIwZvXR4e6k29/FzfFPg2FLvwQKBgQDmuUio+wf1KYj9ZfDOEJ1curBOxcwqVSolJuPxCvBw6vr1YRVpZwgT6uiaaTNPkQVYLA3VJldWqCT1Jg3HdQtdu3D8ILeYsIguWX6o4trcXVQ8bl4kL/1OxSr3mNNti6Fm+qPxFWr3CXqZdNLzsw+iqeaOjfyEKhAgQz68vbau+QKBgQDDbUGQtkb5CJwrBF4rRQhDwFsesgtLvraTfZReThPnCFBjPhUklXkJfbMPQSqmbV2TCIWSFPNaF4xtRxwJv5WFBcaYIihO4udNDHJdGLG001H61hH7uzdIEiFOIMk21/ssvEOSvq0DzGpa4EZh2BKGfL3lZSomr+FXE1EVPyBtWQKBgQDOOXp6n7ZjKbbqV72/UhZ1leCU6DMu+2RqRLKk9tQm+cFKpnYO+iq8MtGpgrJ8QfF2+Bl9FYInUK+H93WFfFamdy3sDgpMji2K0C9oISzinwD16uh2i/Zr+8wF9zGzzoR7mTgD+tLa8DwHAea0MK6fBCGx9bNpJne3fqOK7D9ncQKBgFXoz6vYFXcgi9Z1QxhubmWqR+REjKatngFUcUbLuAAWLZCTGCRO+Ths46xPD3q+FSav1lgTka8HLxpS6kZ2zDoAAf+a9HFJWjkKwbOfSW+efilwtdMdx/2MystXqlzLsYVG9cWqwNAsqoxsj0QwAmTQrYIMaO084CixOzHF4HFxAoGBANHPf3c3pPZr6b9BxJo+y2YqX8+zsoTGJSGJ92QBbj/gpaIF96LQ6OkA4xw6oS35KzdXmccZGBx7NZDUZyMQNq4PmPkguNVbNrx4hp+JwtOf+vl1YJZyjVO3aq4JJ8FoF6ih09mLHlX8hAKf35l/9BOZche5iWHJAdpq9E5Xzy+q";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjxW4NQ6NEbX0wIRkoW2UDN4lU8JEGKzzdHr3gwnFwkvEZigzQZc5vMAW3oARp+eGIBWORtlD3LIvb8gDjk5oocHzxSJISCc+Kd6FVf0M99W/ubgWlsurKyUNn10amB0IE3YEVa/hrX/C4QsoXxCp6qToiE8IxcXcM+NJLSCP4UBFCkH+FO7dm3cH2iq6CSsKCQ6YafhTH/4ENBAYT6H9TdsrcvN5u7vCjvjG6yyN3X1O2cZ9QLDeirROZqYLDVF/VTeMLtGSPbJgmAHbMMiHoP/Zr46NVer8PXYVULgl8jiVZ+pOGcZ/uZiYLAGH3jMnbL4M29ZGjqkhs5q7H2tsnQIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private String notify_url;

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private String return_url = "http://member.gulimall.com/memberOrder.html";

    // 签名方式
    private String sign_type = "RSA2";

    // 字符编码格式
    private String charset = "utf-8";

    //超时时间
    private String timeout = "30m";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\"" + out_trade_no + "\","
                + "\"total_amount\":\"" + total_amount + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"body\":\"" + body + "\","
                + "\"timeout_express\":\"" + timeout + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应：" + result);

        return result;

    }


    public String demo() throws AlipayApiException {
        String privateKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCwIZpUHx1psDqkZvFR5DhQMnLif2gJ13ximv3bIw2q2awo/NrCN1Bpznd85kkolc6+4PY2pCnhEwd3NlEJrI2vRdqvEQ6qFs9dhhXo8x8BP5eXCcnMeZbRIVz1rJMGVrM+zarBBf/yFPTyv69lBcoSOWdaULuDewtf8bbOSBa0QkwP6U3I0BUwr6Uk+bSGIyZkQVQc0wC2xmHaP85+eMvbGvba/QpxOuDktAJCDwvSQQZ41tUXgwCvppixo2jotip+xex6qceujexo3jNGRe/6HGG4xyaRZskiEzc7A9L0WWpe3w9fg83wExy51Zfu/hLIm/g6dMsZkRwy+7bjItmRAgMBAAECggEAHdmkESuwzW+rkJ9NuMZH8PeL1u0m0bwufXutGE2JjHIVpmCsJow/WZ8/SvCwZ2t0PY9cywfpfRa/himzdENFQ/Y/yLnIVXVGMaB+6BsEHy8K3vMuuzPEJq9T1OMpi8J34GX00X/Vz85TcSyhaiqgA5Ss+naCQRdCZ0FM7Qr0phnqFuYhPpaJ6EvKNcynquErJ8oVy9JBvbfUhKPJ3awt1JSwSY0zwIiXI9nwccz2WRDkaB5RaSg2DAHv74z38/JDVVHhi5rwG0h+eLN/oJFoCha3vT3eh54sPdYXqgm1r5wBeTgP81IT1fUKrX/mdIwZvXR4e6k29/FzfFPg2FLvwQKBgQDmuUio+wf1KYj9ZfDOEJ1curBOxcwqVSolJuPxCvBw6vr1YRVpZwgT6uiaaTNPkQVYLA3VJldWqCT1Jg3HdQtdu3D8ILeYsIguWX6o4trcXVQ8bl4kL/1OxSr3mNNti6Fm+qPxFWr3CXqZdNLzsw+iqeaOjfyEKhAgQz68vbau+QKBgQDDbUGQtkb5CJwrBF4rRQhDwFsesgtLvraTfZReThPnCFBjPhUklXkJfbMPQSqmbV2TCIWSFPNaF4xtRxwJv5WFBcaYIihO4udNDHJdGLG001H61hH7uzdIEiFOIMk21/ssvEOSvq0DzGpa4EZh2BKGfL3lZSomr+FXE1EVPyBtWQKBgQDOOXp6n7ZjKbbqV72/UhZ1leCU6DMu+2RqRLKk9tQm+cFKpnYO+iq8MtGpgrJ8QfF2+Bl9FYInUK+H93WFfFamdy3sDgpMji2K0C9oISzinwD16uh2i/Zr+8wF9zGzzoR7mTgD+tLa8DwHAea0MK6fBCGx9bNpJne3fqOK7D9ncQKBgFXoz6vYFXcgi9Z1QxhubmWqR+REjKatngFUcUbLuAAWLZCTGCRO+Ths46xPD3q+FSav1lgTka8HLxpS6kZ2zDoAAf+a9HFJWjkKwbOfSW+efilwtdMdx/2MystXqlzLsYVG9cWqwNAsqoxsj0QwAmTQrYIMaO084CixOzHF4HFxAoGBANHPf3c3pPZr6b9BxJo+y2YqX8+zsoTGJSGJ92QBbj/gpaIF96LQ6OkA4xw6oS35KzdXmccZGBx7NZDUZyMQNq4PmPkguNVbNrx4hp+JwtOf+vl1YJZyjVO3aq4JJ8FoF6ih09mLHlX8hAKf35l/9BOZche5iWHJAdpq9E5Xzy+q";
        String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjxW4NQ6NEbX0wIRkoW2UDN4lU8JEGKzzdHr3gwnFwkvEZigzQZc5vMAW3oARp+eGIBWORtlD3LIvb8gDjk5oocHzxSJISCc+Kd6FVf0M99W/ubgWlsurKyUNn10amB0IE3YEVa/hrX/C4QsoXxCp6qToiE8IxcXcM+NJLSCP4UBFCkH+FO7dm3cH2iq6CSsKCQ6YafhTH/4ENBAYT6H9TdsrcvN5u7vCjvjG6yyN3X1O2cZ9QLDeirROZqYLDVF/VTeMLtGSPbJgmAHbMMiHoP/Zr46NVer8PXYVULgl8jiVZ+pOGcZ/uZiYLAGH3jMnbL4M29ZGjqkhs5q7H2tsnQIDAQAB";
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setServerUrl("https://openapi.alipaydev.com/gateway.do");
        alipayConfig.setAppId("2021000122615879");
        alipayConfig.setPrivateKey(privateKey);
        alipayConfig.setFormat("json");
        alipayConfig.setAlipayPublicKey(alipayPublicKey);
        alipayConfig.setCharset("UTF8");
        alipayConfig.setSignType("RSA2");
        AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo("20150320010101001");
        model.setTotalAmount("88.88");
        model.setSubject("Iphone6 16G");
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        request.setBizModel(model);
        AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
        System.out.println(response.getBody());
        if (response.isSuccess()) {
            System.out.println("调用成功");
            return response.getBody();
        } else {
            System.out.println("调用失败");
            return response.getBody();
        }
    }
}
