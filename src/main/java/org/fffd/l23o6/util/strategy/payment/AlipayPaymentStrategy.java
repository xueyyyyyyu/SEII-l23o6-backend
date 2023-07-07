package org.fffd.l23o6.util.strategy.payment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class AlipayPaymentStrategy extends PaymentStrategy{

    private static final String ALIPAY_GATEWAY = "https://openapi.alipay.com/gateway.do";
    private static final String APP_ID = "YOUR_APP_ID";
    private static final String APP_PRIVATE_KEY = "YOUR_APP_PRIVATE_KEY";
    private static final String CHARSET = "UTF-8";
    private static final String SIGN_TYPE = "RSA2";

    public static void main(String[] args) {
        // 生成支付订单
        String outTradeNo = "ORDER123456789";
        String totalAmount = "10.00";
        String subject = "Test Order";
        String body = "This is a test order.";

        String paymentUrl = generatePaymentOrder(outTradeNo, totalAmount, subject, body);
        System.out.println(paymentUrl);

        // 处理支付结果通知
        // 你可以在支付结果通知接口中获取到相关参数，并进行处理
        // 确认支付结果的准确性，并完成相关业务逻辑
    }

    // 生成支付订单
    public static String generatePaymentOrder(String outTradeNo, String totalAmount, String subject, String body) {
        Map<String, String> commonParams = new HashMap<>();
        commonParams.put("app_id", APP_ID);
        commonParams.put("method", "alipay.trade.page.pay");
        commonParams.put("charset", CHARSET);
        commonParams.put("sign_type", SIGN_TYPE);
        commonParams.put("timestamp", String.valueOf(System.currentTimeMillis()));
        commonParams.put("version", "1.0");
        commonParams.put("notify_url", "YOUR_NOTIFY_URL");

        Map<String, String> bizContent = new HashMap<>();
        bizContent.put("out_trade_no", outTradeNo);
        bizContent.put("total_amount", totalAmount);
        bizContent.put("subject", subject);
        bizContent.put("body", body);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

        commonParams.put("biz_content", toJsonString(bizContent));

        String sign = signRequest(commonParams);
        commonParams.put("sign", sign);

        StringBuilder paymentUrlBuilder = new StringBuilder();
        paymentUrlBuilder.append(ALIPAY_GATEWAY).append("?");

        for (Map.Entry<String, String> entry : commonParams.entrySet()) {
            try {
                paymentUrlBuilder.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), CHARSET))
                        .append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return paymentUrlBuilder.toString();
    }

    // 对请求参数进行签名
    private static String signRequest(Map<String, String> params) {
        try {
            String signContent = getSignContent(params);
            PrivateKey privateKey = getPrivateKey(APP_PRIVATE_KEY);
            Signature signature = Signature.getInstance("SHA256WithRSA");
            signature.initSign(privateKey);
            signature.update(signContent.getBytes(CHARSET));
            byte[] signBytes = signature.sign();
            return new String(Base64.getEncoder().encode(signBytes), CHARSET);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 获取待签名的字符串
    private static String getSignContent(Map<String, String> params) {
        StringBuilder content = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                content.append("&");
            }
            content.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
        return content.toString();
    }

    // 根据私钥字符串获取私钥对象
    private static PrivateKey getPrivateKey(String privateKey) throws Exception {
        privateKey = privateKey.replace("-----BEGIN PRIVATE KEY-----\n", "");
        privateKey = privateKey.replace("-----END PRIVATE KEY-----", "");
        byte[] keyBytes = Base64.getDecoder().decode(privateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    // 将Map转换为JSON字符串
    private static String toJsonString(Map<String, String> map) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!first) {
                jsonBuilder.append(",");
            }
            jsonBuilder.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
            first = false;
        }
        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }
}

