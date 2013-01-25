package com.example.scanwifi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlSerializer;

import android.net.wifi.ScanResult;
import android.util.Log;
import android.util.Xml;

public class StoreInfo {
	private List<List<ScanResult>> m_allResult;
	private int m_frequency;
	private long m_time;
	private Calendar m_startTime;
	private String m_position;
	
	private File m_xmlfile;
	private OutputStream m_os;
	private XmlSerializer m_serializer = Xml.newSerializer();
	
	private Map<String, String> m_bssid_ssid;//���mac��ַ��AP����
	private Map<String, List<String>> m_bssid_rssi;//���mac��ַ�����AP���յ�������RSSIֵ������list��
	
	String tag = "store";
	
	public StoreInfo(int fren, long time, Calendar start, String position) throws IllegalArgumentException, IllegalStateException, IOException{
		Log.v(tag, "xml");
		m_allResult = new ArrayList<List<ScanResult>>();
		m_frequency = fren;
		m_time = time;
		m_startTime = start;
		m_position = position;
		
		m_xmlfile = new File("/sdcard/positon.xml");
		boolean isExists = m_xmlfile.exists();
		if( !isExists ) {
			m_xmlfile.createNewFile();
        } 
		Log.v(tag, "xml");
		
		m_os = new FileOutputStream(m_xmlfile,true);
		
		m_serializer.setOutput(m_os, "UTF-8");
		m_serializer.startDocument("UTF-8", true);
		
		Log.v(tag, "xml");
		m_bssid_ssid = new HashMap<String, String>();
		m_bssid_rssi = new HashMap<String, List<String>>();
		
	}

	public void addResultToList(List<ScanResult> l){
		m_allResult.add(l);
	}
	
	public List<ScanResult> getListWithIndex(int i){
		return m_allResult.get(i);
	}
	
	public int getListLen(){
		return m_allResult.size();
	}
	
	public void writeXml() throws IOException{
		

//		public static void savePositions (OutputStream os,String position) throws Exception{
		
		m_serializer.startTag(null, "scan");
		//=====д��ʼʱ�䣬Ƶ�ʣ�����ʱ�䣬����λ��==============
		m_serializer.startTag(null, "startTime");
		m_serializer.text(WriteFile.tranTimeToString(m_startTime));
		m_serializer.endTag(null, "startTime");
		
		m_serializer.startTag(null, "frequency");
		m_serializer.text(String.valueOf(m_frequency));
		m_serializer.endTag(null, "frequency");
		
		m_serializer.startTag(null, "duration");
		m_serializer.text(String.valueOf(m_time));
		m_serializer.endTag(null, "duration");
		
		m_serializer.startTag(null, "position");
		m_serializer.text(m_position);
		m_serializer.endTag(null, "position");
		
		//==========дÿ��RSSI��ֵ
		getApAllRSSI();
		Iterator<String> iBssid = m_bssid_ssid.keySet().iterator();
		Log.v(tag, "rssi");
		while(iBssid.hasNext()){
			String bssid = iBssid.next();
			
			//============дAP����========================
			m_serializer.startTag(null, "ssid");
			m_serializer.text(m_bssid_ssid.get(bssid));
			m_serializer.endTag(null, "ssid");

			//============дAP mac��ַ========================
			m_serializer.startTag(null, "bssid");
			m_serializer.text(bssid);
			m_serializer.endTag(null, "bssid");
		
			Log.v(tag, "rssi");
			//============д���AP���յ�������rssiֵ========================
			m_serializer.startTag(null, "rssi");
			List<String> thisApRssi = m_bssid_rssi.get(bssid);
			Iterator<String> iRssi = thisApRssi.iterator();
			while(iRssi.hasNext()){
				m_serializer.text(iRssi.next()+"\r\n");
			}
			m_serializer.endTag(null, "rssi");
			
			Log.v(tag, "rssi");
		}

		m_serializer.endTag(null, "scan");
		m_serializer.text("\r\n");
		m_serializer.endDocument();
		
		m_os.flush();
		m_os.close();
//	}
	}
	
	//==========��ÿ��AP��RSSI��list����������AP��mac��ַ����map========
	private void getApAllRSSI(){
		m_bssid_ssid.clear();
		m_bssid_rssi.clear();
		
		Iterator<List<ScanResult>> iList = m_allResult.iterator();
		while (iList.hasNext()) {
			List<ScanResult> nextList = iList.next();
			
			Iterator<ScanResult> iscan = nextList.iterator();
			
			while(iscan.hasNext()){
				ScanResult nextScan = iscan.next();
				
				boolean isHas = m_bssid_ssid.containsKey(nextScan.BSSID);
				if(isHas == false){//�����������AP������
					List<String> oneApRssi = new ArrayList<String>();
					m_bssid_ssid.put(nextScan.BSSID, nextScan.SSID);
					
					oneApRssi.add(String.valueOf(nextScan.level));
					m_bssid_rssi.put(nextScan.BSSID, oneApRssi);
				}
				else{//���AP�Ѿ����ֹ���
					List<String> thisApRssi = m_bssid_rssi.get(nextScan.BSSID);
					thisApRssi.add(String.valueOf(nextScan.level));
				}
			}
		}
	}
}

