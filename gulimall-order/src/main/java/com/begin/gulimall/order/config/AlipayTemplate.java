package com.begin.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.begin.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")//表示以下所有配置都可以和properties文件绑定
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2021000118613916";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key ="MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCGserLeU5Y1k3uo0OVIZjUbC+PtVkVuU9dGKj2LqXwCIPPB2SefHwy4w4l2Jq2m/7LgW9ccjIi10akLmX9JC76MFxOjFab2zLy8At9z7qKu0gnpqxpB5JvTqLDUrZl4U/Pmnxdluq44JMd565ef6T5QqlYu8IZkTTx5P4hKQjd+13J/x+TUXBxfLAc67OM2bXGje5plZBdScIvHDvO3a5NrdVl9E3Jpq/xZFpfyCET4jsyzQnU7XFBnaHLaJEYfxr5G9x9jxjgzGKBNLGb9xKDQDQVtLYNxdQSvrbHerR6VsJWRZqNTYJeE0zYY+dWMSuqXB397/ve62joq5+HqqQ5AgMBAAECggEAJU+fhKGjrEQg7lvXWrYlyaH2Qs6vUK82Zslv9FHTsXwKVwzrOGOvW8E9qAoqQS9REzpViLwxWyla7hvUMJ0XTkKbLz78tsuvf1AzPrSOLePXBvTKiYWJcL/NomGFGQ+ithVLIfmuJ2w+FGibCp49KrbmhifsinyIuAEAzFq7lBGnnSfxQPsnlSmeFdXD+uUgXaYPeeufhTvy1JpEpghd3PUYdXFkm0rvaMCinc56+EI/AkO8LQh2Xs6Q8rFE90qZsv8oy8Q2Le71P53zJgzBPT1dBbNPVVyY6ZQcFOOT8s7TKnJASDOary7VhgR8LAfSC4k6S1b3LIxqb35sK3UZIQKBgQDTJndOVVxLhBHQILw4OAAH+hPVqoP3x4VF/AfN7iTWNlXoHl9qPa2MWczVPSHmParNaG7c8svq437+Ze/en3tlbCoIRjvtkLSdGSpo4loTWaDONB1O6Xz0ujShBU8z6du7HyGkm/ykk5pa8F4iM0F73nxwI0c6WZp0clywNC9kbQKBgQCjThwE5a0Mji/MyVZZKwmienTHUvqP27DMZsn0iyrmLJEhj4epL03Z9Ug4wUbOwHuiaQ53je+AOsvPHyhvkSJhV3WY1d5ork21AyP+m2/wKJlNagFgMb0QyJN7P15XNo7a+ZTKj0/BAzlWPbtW2tlgf4m/G0E+PfCZXgNpXOInfQKBgAH9uN5KO8aShkB6LowxKv7oQsufAgOSJBWW3NP6TOqgNIstziSIVFygdCgGnY04Q6YQOAtpVrbuUkI/39Uvl9BLBtJxJxuEY8+fbyDyEXthOvz2zNfzp7P/FYP3MtoAeIv5LS4KMoWyxp/Y6GpWJWXvoSjFjGRmd+hlM66Nr2y5AoGAAgpw2TLRlc2VFVcSyCx8g1ZKbU9BKFkTfYpvTaoLkDLQYX38sQWRUH77NxJbQywun1n2v1G7w3t4no8CH1rN9i90ncNWczJycCeo5H6myHaJa+k+OSiF91a6WRjyod5hrKTf0iQ2V/AlYEmHNM+pVcZiP0wk5ZU6k4AURNXPUbUCgYEAxahjapEYYe6mp23VWfVA7WtEJizvhBupKFWm3OwXJ2zsiGr051Qzgzr5Xh+aSfQQwIiCaexFAY3OXn6/Petj5GgZNHvPkOZ/hlxRaU4QYzhnum9leNXFaoIl2Skn7wSNSvmBnOSHtPXwyZ20Roz/SfoBvt4aByRAhKSFmLwepx8=";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjUS7As/jeW8JQRX7K99XiH3hiGR2+xcxUKYtbYYgqIBNWdds+XFmWMjK/Lzj4MfrtcB+WzjVTrHb2ptZRCRrFen2AOLMecq574P0k72+cYkZq/BHR/6zB8vOs9Rr8qWbvQbUIXE6OnXa9fD5MTuSlZq6xaNUFtfZdtxHtaqpqDPcWf4D/qqs7ShjA3p0VXT/5Ckc/GShwWcZ5wjOSYeELGXs/kDAF0E4Re7VsZjz1QuuZFBDWiA7mpbriBjB7COfOuZXgqByUQEJXNKUvs2ELxYuk6opuAeGONaeW3857QwZNBKLIw0uGmYvwfa+7b5sZ4NwbOVBZMrA0q9iCjdoHwIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url="http://1a3cwrbo17.52http.tech/payed/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url="http://member.gulimall.com:8000/memberOrder.html";

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    //自动收单时间
    private String timeout="30m";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

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

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\""+timeout+"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
