package com.seagen.ecc.ectcps.msc;

public class DeviceConst {
	
	public final static long CMD_SPAN_TIME = 20;	//获取返回命令的时间间隔

	public final static String RETURN_RESULT = "Result";
	public final static String RETURN_CODE = "ReturnCode";
	
	public final static String RETURN_CODE_SUCCESS = "1";	//返回结果：正常/正确/成功
	public final static String RETURN_CODE_FAILURE = "0";	//返回结果：异常/错误/失败
	
	public final static String MODULE_NAME_CABINET   = "Cabinet";		//模块名称：副柜
	public final static String MODULE_NAME_USB_CAME  = "USBCamera";		//模块名称：主柜相机
	public final static String MODULE_NAME_CASHIER   = "Cashier";		//模块名称：收钞机
	public final static String MODULE_NAME_BAR_CODE  = "BarCode";	 	//模块名称：条码扫描仪
	public final static String MODULE_NAME_CARD_SEN  = "CardSender";	//模块名称：发卡器
	public final static String MODULE_NAME_IDRF_CARD = "IDRFCard";		//模块名称：读卡器
	public final static String MODULE_NAME_PRINTER   = "Printer";		//模块名称：打印机
	public final static String MODULE_NAME_VIDEO     = "Video";			//模块名称：摄像头
	

	public final static int FUN_CODE_DEFAULT = 1;		//功能代码：默认
	
	public final static int FUN_CODE_CELL_OPEN = 11;	//功能代码：打开格口
	public final static int FUN_CODE_CELL_QUER = 12;	//功能代码：查询格口状态
	
	public final static int FUN_CODE_OPEN_CAMER  = 20;	//功能代码：打开摄像头
	public final static int FUN_CODE_SHOOT_PHPTO = 21;	//功能代码：拍照
	public final static int FUN_CODE_CLOSE_CAMER = 29;	//功能代码：关闭摄像头
	
	public final static int FUN_CODE_CASH_STA = 31;		//功能代码：收钞
	public final static int FUN_CODE_CASH_END = 32;		//功能代码：停止收钞
	
	public final static int FUN_CODE_OPEN_SCAN = 40;	//功能代码：打开扫描仪
	public final static int FUN_CODE_SCAN_BAR  = 41; 	//功能代码：扫描条码
	public final static int FUN_CODE_CLOSE_SCAN= 49;	//功能代码：关闭扫描仪
	
	public final static int FUN_CODE_CARD_SENT = 51;	//功能代码：发卡
	
	public final static int FUN_CODE_OPEN_READ = 60;	//功能代码：打开读卡器
	public final static int FUN_CODE_READ_CARD = 61;	//功能代码：读卡
	public final static int FUN_CODE_CLOSE_READ= 69;	//功能代码：关闭读卡器
	
	public final static int FUN_CODE_PRINT = 71;		//功能代码：打印
	
	public final static int FUN_CODE_VIDEO_STA = 81;	//功能代码：开始录像
	public final static int FUN_CODE_VIDEO_END = 82;	//功能代码：结束录像
	
	public final static String CELL_SWITCH_OFF = "1";	//格口状态：关
	public final static String CELL_SWITCH_ON  = "2";	//格口状态：开
}