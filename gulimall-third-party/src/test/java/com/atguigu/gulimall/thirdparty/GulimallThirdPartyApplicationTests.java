package com.atguigu.gulimall.thirdparty;


import com.aliyun.oss.OSSClient;
import com.atguigu.gulimall.thirdparty.component.SmsComponent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallThirdPartyApplicationTests {
	@Autowired
	OSSClient ossClient;

	@Autowired
	SmsComponent smsComponent;
	@Test
	public void contextLoads() throws FileNotFoundException {
		InputStream inputStream = new FileInputStream("C:\\Users\\yfl42\\Pictures\\aaa.png");
		ossClient.putObject("gulimall-yfl423","ahaha.png",inputStream);
		ossClient.shutdown();
		System.out.println("上传完成");
	}
	@Test
	public void testSms(){
		smsComponent.sendSms("15737532226","13579246810");
	}
}
