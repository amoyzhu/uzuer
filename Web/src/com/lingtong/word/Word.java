package com.lingtong.word;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;

import org.apache.commons.io.FilenameUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.lingtong.model.Contract;
import com.lingtong.model.Tenants;
import com.lingtong.util.RMBUtil;
import com.lingtong.util.SystemConfiguration;
import com.lingtong.vo.ContractVo;
import com.lingtong.vo.RoomVo;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class Word {
	public static String createContractDoc(Contract contract, RoomVo roomVo,
			Tenants tenants) {
		Configuration configuration = new Configuration();
		configuration.setDefaultEncoding("utf-8");
		Map dataMap = new HashMap();
		getContractMap(dataMap, contract, roomVo, tenants);

		configuration.setClassForTemplateLoading(Word.class,
				"/com/lingtong/ftl");
		Template t = null;
		try {
			t = configuration.getTemplate("RoomContract20150915.ftl");
			t.setEncoding("utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("wor : " + Word.class.getResource("/"));
		URL base = Word.class.getResource("");

		try {
			System.out.println(base.getFile());
			String path = new File(base.getFile(), "../../../../../tempdoc")
					.getCanonicalPath();
			String filePath = path + "/test" + System.currentTimeMillis()
					+ ".doc";
			System.out.println("path : " + path);

			File outFile = new File(filePath);
			Writer out = null;

			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outFile), "utf-8"));

			t.process(dataMap, out);
			out.close();
			return filePath;
		} catch (TemplateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "ERROR";
	}
	
	public static String createContractDoc(ContractVo contractVo) {
		File outFile = null;
		Configuration configuration = new Configuration();
		configuration.setDefaultEncoding("utf-8");
		Map dataMap = new HashMap();
		getContractMap(dataMap, contractVo);

		configuration.setClassForTemplateLoading(Word.class,
				"/com/lingtong/ftl");
		Template t = null;
		try {
			t = configuration.getTemplate("RoomContract1026.ftl");
			t.setEncoding("utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("wor : " + Word.class.getResource("/"));
		//URL base = Word.class.getResource("");

		try {
			/* 
			System.out.println(base.getFile());
			String path = new File(base.getFile(), "../../../../../tempdoc")
					.getCanonicalPath();
			String filePath = path + "/test" + System.currentTimeMillis()
					+ ".doc";*/
			//ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
			//System.out.println("contractPath:"+SystemConfiguration.getInstance().getString("contractPath"));
			String filePath = FilenameUtils.concat(SystemConfiguration.getInstance().getString("ssq.contractPath"), System.currentTimeMillis() + ".doc");
			System.out.println("path : " + filePath);

			outFile = new File(filePath);
			Writer out = null;

			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outFile), "utf-8"));

			t.process(dataMap, out);
			out.flush();
			out.close();
			return filePath; 
		} catch (TemplateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "ERROR";
	}
	
	private static void getContractMap(Map dataMap, Contract contract,
			RoomVo roomVo, Tenants tenants) {

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();

		dataMap.put("contractno", contract.getContractno());
		dataMap.put("a_address", "杭州市西湖区");
		dataMap.put("tenantname", tenants.getFirst_name()+tenants.getLast_name());
		dataMap.put("tenantidcard", tenants.getId_card());
		dataMap.put("b_address", roomVo.getCityid());
		dataMap.put("tenant_tel", tenants.getTel_number());
		dataMap.put("room_address", roomVo.getCityid());
		dataMap.put("room_area", roomVo.getSize());

		try {
			Date starttime = format.parse(contract.getSign_time());

			Date endtime = format.parse(contract.getEnd_time());

			calendar.setTime(starttime);
			dataMap.put("start_year", calendar.get(Calendar.YEAR) +"");
			dataMap.put("start_month", calendar.get(Calendar.MONTH) +"");
			dataMap.put("start_day", calendar.get(Calendar.DAY_OF_MONTH) +"");
			calendar.setTime(endtime);
			dataMap.put("end_year", calendar.get(Calendar.YEAR) +"");
			dataMap.put("end_month", calendar.get(Calendar.MONTH) +"");
			dataMap.put("end_day", calendar.get(Calendar.DAY_OF_MONTH) +"");
			calendar.setTime(starttime);

		} catch (ParseException e) {
			e.printStackTrace();
		}

		dataMap.put("room_price", roomVo.getPrice());
		Double price = roomVo.getPrice() + 0.0D;
		dataMap.put("room_price_big", RMBUtil.toBigAmt(price));

		switch (contract.getContract_type_id()) {
		case 1:
			dataMap.put("contracttype", "押一付三");
			dataMap.put("byctype", "四");
			break;
		case 2:
			dataMap.put("contracttype", "分期支付");
			dataMap.put("byctype", "一");
			break;
		}

		dataMap.put("bankname", "招商银行股份有限公司杭州高新支行");
		dataMap.put("bankperson", "杭州溯品信息科技有限公司");
		dataMap.put("bankno", "571908610010701");

		dataMap.put("create_year", calendar.get(Calendar.YEAR) +"");
		dataMap.put("create_month", (calendar.get(Calendar.MONTH) + 1)+"");
		dataMap.put("create_day", calendar.get(Calendar.DAY_OF_MONTH) +"");

	}
	
	private static void getContractMap(Map dataMap, ContractVo contractVo) {

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();

		dataMap.put("contractno", contractVo.getContractno());
		dataMap.put("a_address", "杭州市西湖区");
		dataMap.put("tenantname", contractVo.getFirst_name()+contractVo.getLast_name());
		dataMap.put("tenantidcard", contractVo.getId_card());
		dataMap.put("b_address", contractVo.getCityid());
		//dataMap.put("tenant_tel", contractVo.getTel_number());
		dataMap.put("tenant_tel", contractVo.getFirst_name()+contractVo.getLast_name());
		dataMap.put("room_address", contractVo.getCityid());
		dataMap.put("room_area", contractVo.getSize());
		dataMap.put("a_fullname", "杭州溯品信息科技有限公司" );
		dataMap.put("b_fullname", contractVo.getFirst_name()+contractVo.getLast_name());
		try {
			System.out.println(contractVo.getSign_time());
			Date starttime = format.parse(contractVo.getSign_time());

			Date endtime = format.parse(contractVo.getEnd_time());

			calendar.setTime(starttime);
			dataMap.put("start_year", calendar.get(Calendar.YEAR) +"");
			dataMap.put("start_month", calendar.get(Calendar.MONTH)+1 +"");
			dataMap.put("start_day", calendar.get(Calendar.DAY_OF_MONTH) +"");
			calendar.setTime(endtime);
			dataMap.put("end_year", calendar.get(Calendar.YEAR) +"");
			dataMap.put("end_month", calendar.get(Calendar.MONTH)+1 +"");
			dataMap.put("end_day", calendar.get(Calendar.DAY_OF_MONTH) +"");
			calendar.setTime(starttime);

		} catch (ParseException e) {
			e.printStackTrace();
		}

		dataMap.put("room_price", contractVo.getPrice());
		dataMap.put("yajin",  contractVo.getPrice());
		dataMap.put("yajin2",  contractVo.getPrice());
		dataMap.put("dingjin",  0);
		dataMap.put("yuanyajinbili", "100%");
		dataMap.put("bushou", 0);
		Double price = contractVo.getPrice() + 0.0D;
		dataMap.put("room_price_big", RMBUtil.toBigAmt(price));

		switch (contractVo.getContract_type_id()) {
		case 1:
			dataMap.put("contracttype", "押一付三");
			dataMap.put("byctype", "四");
			break;
		case 2:
			dataMap.put("contracttype", "分期支付");
			dataMap.put("byctype", "一");
			break;
		}
//		dataMap.put("bankname", "招商银行股份有限公司杭州高新支行");
//		dataMap.put("bankperson", "杭州溯品信息科技有限公司");
//		dataMap.put("bankno", "571908610010701");
		dataMap.put("create_year", calendar.get(Calendar.YEAR) +"");
		dataMap.put("create_month", (calendar.get(Calendar.MONTH) + 1) +"");
		dataMap.put("create_day", calendar.get(Calendar.DAY_OF_MONTH) +"");

	}

	public static void main(String[] args) {
		System.out.println( Word.class.getResource(""));
		URL base = Word.class.getResource("");
		String path;
		try {
			path = new File(base.getFile(), "../../../../../contract")
			.getCanonicalPath();
			System.out.println(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
