package com.casetool.utils;

import com.google.common.io.Files;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.CustomIndexedColorMap;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.IndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.Color;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class DatasetsUtils {
    private final static String splitText = "S_P_L_I_T";
	private final static String sheetRowSplit = "R_S_P_L_I_T";
	private final static String sheetColumnSplit = "C_S_P_L_I_T";
	private final static String lineSplit = "L_S_P_L_I_T";

    public static void writeCSV(String csvFilePath, String data, boolean appendWrite){
        File csvFile = new File(csvFilePath);
        try {
            if(!csvFile.exists()){
                csvFile.createNewFile();
            }

            Files.write(data.replace(splitText,", ").getBytes("GBK"), csvFile);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void writeExcel(String excelFile, String sheetName, String data, boolean appendWrite){
        try {
            SXSSFWorkbook wb;
            if(appendWrite){
                XSSFWorkbook xwb = new XSSFWorkbook(new FileInputStream(excelFile));
                wb = new SXSSFWorkbook(xwb, 100);
            }else {
                wb = new SXSSFWorkbook(100);
            }
            Sheet sh = wb.createSheet(sheetName);
            String[] rowDatas =data.split(lineSplit);
            int rowNum = rowDatas.length;
            int colNum = rowDatas[0].split(splitText).length;
            
            
            
            CellStyle cellStyle = wb.createCellStyle();
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            
            for(int i = 0; i < rowNum; i++){
                Row row = sh.createRow(i);
                for(int j = 0; j < colNum; j++){
                    Cell cell = row.createCell(j);
                    //生成案例序号设置公式=ROW()-1
                    if(j == 3 && i > 0) {
                    	cell.setCellFormula(rowDatas[i].split(splitText, -1)[j]);
                    	
                    }else {
                    	cell.setCellValue(rowDatas[i].split(splitText, -1)[j]);
                    }
                  //测试步骤自动换行待完成
                    if(j == 7 && i > 0) {
                    	cellStyle.setWrapText(true);
                    	cell.getRow()
                    	  .setHeightInPoints(cell.getSheet().getDefaultRowHeightInPoints()*2);
                    	
                    }
                    cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                    cell.setCellStyle(cellStyle);
                    
                }
            }
            
            
            wb.getSheet(sheetName).setZoom(70);
            wb.getSheet(sheetName).getRow(0).setHeightInPoints(28.4f);
            XSSFFont font = (XSSFFont) wb.createFont();
            font.setFontHeightInPoints((short) 11);
            font.setFontName("宋体");
            font.setBold(true);
            // Background color
            XSSFColor color1 = new XSSFColor(new Color(83, 141, 213),new DefaultIndexedColorMap()); 
            XSSFCellStyle style1=(XSSFCellStyle) wb.createCellStyle();
            style1.setFillForegroundColor(color1);
            style1.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style1.setFont(font);
            style1.setBorderBottom(BorderStyle.THIN);
            style1.setBorderTop(BorderStyle.THIN);
            style1.setBorderLeft(BorderStyle.THIN);
            style1.setBorderRight(BorderStyle.THIN);
            style1.setVerticalAlignment(VerticalAlignment.CENTER);
            style1.setAlignment(HorizontalAlignment.CENTER);
            
            XSSFColor color2 = new XSSFColor(new Color(217, 217, 217),new DefaultIndexedColorMap()); 
            XSSFCellStyle style2=(XSSFCellStyle) wb.createCellStyle();
            style2.setFillForegroundColor(color2);
            style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style2.setFont(font);
            style2.setBorderBottom(BorderStyle.THIN);
            style2.setBorderTop(BorderStyle.THIN);
            style2.setBorderLeft(BorderStyle.THIN);
            style2.setBorderRight(BorderStyle.THIN);
            style2.setVerticalAlignment(VerticalAlignment.CENTER);
            style2.setAlignment(HorizontalAlignment.CENTER);
            
            int[] colorIndexArray = new int[] {0, 1, 2, 9, 10};
            ArrayList<Integer> colorLists = new ArrayList<>();
            for (int i = 0; i < colorIndexArray.length; i++) {
				colorLists.add(colorIndexArray[i]);
			}
            for (int i = 0; i < colNum; i++) {
				if(colorLists.contains(i)) {
					wb.getSheet(sheetName).getRow(0).getCell(i).setCellStyle(style1);
				}else {
					wb.getSheet(sheetName).getRow(0).getCell(i).setCellStyle(style2);
				}
			}
            

            
			for (int i = 0; i <= colNum; i++) {
				if(i == 0) {
					wb.getSheet(sheetName).setColumnWidth(i, 12*256);
				}else if(i == 1) {
					wb.getSheet(sheetName).setColumnWidth(i, 18*256);
				}else if(i == 2) {
					wb.getSheet(sheetName).setColumnWidth(i, 28*256);
				}else if(i == 3) {
					wb.getSheet(sheetName).setColumnWidth(i, 4*256);
				}else if(i == 4) {
					wb.getSheet(sheetName).setColumnWidth(i, 6*256);
				}else if(i == 5) {
					wb.getSheet(sheetName).setColumnWidth(i, 45*256);
				}else if(i == 6) {
					wb.getSheet(sheetName).setColumnWidth(i, 15*256);
				}else if(i == 7) {
					wb.getSheet(sheetName).setColumnWidth(i, 62*256);
				}else if(i == 8) {
					wb.getSheet(sheetName).setColumnWidth(i, 30*256);
				}else if(i == 9) {
					wb.getSheet(sheetName).setColumnWidth(i, 30*256);
				}else if(i == 10) {
					wb.getSheet(sheetName).setColumnWidth(i, 25*256);
				}
			}
			
            FileOutputStream out = new FileOutputStream(excelFile);
            wb.write(out);
            out.close();
            wb.dispose();
			wb.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> readExcel(String excelFile, String sheetName, boolean removeHeader
            , HashMap<String,String> excelConverter
    ){
        ArrayList<String> result = new ArrayList<>();
        try {
            InputStream inp = new FileInputStream(excelFile);
            Workbook wb = WorkbookFactory.create(inp);
            Sheet sheet = wb.getSheet(sheetName);
            int rows = sheet.getLastRowNum();
            int startRow = removeHeader?1:0;
            int cols = sheet.getRow(0).getLastCellNum();
            HashMap<Integer, String> colType = new HashMap<>();
            if(excelConverter != null){
                for (int i = 0; i < cols; i++) {
                    colType.put(i,
                            excelConverter.get(sheet.getRow(0).getCell(i).toString()));
                }
            }
            StringBuilder temp;
            DataFormatter dataFormatter = new DataFormatter();

            for (int i = startRow; i <= rows; i++) {
                temp = new StringBuilder();
                for (int j = 0; j < cols; j++) {
                    if(colType.size()>0) {
                        if (colType.get(j).equals("str")) {
                            temp.append(dataFormatter.formatCellValue(sheet.getRow(i).getCell(j))).append(splitText);
                        } else {
                            temp.append(sheet.getRow(i).getCell(j)).append(splitText);
                        }
                    }else {
                        temp.append(dataFormatter.formatCellValue(sheet.getRow(i).getCell(j))).append(splitText);
                    }
                }
                result.add(temp.toString());
            }
            inp.close();
            wb.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

	public static void mergeMultiSheetsToOne(String oriExcelFile, String newExcelFile){
		try {
			InputStream inp = new FileInputStream(oriExcelFile);
			Workbook wb = WorkbookFactory.create(inp);
			Sheet tempSheet;
			StringBuilder allSheetString = new StringBuilder();
			for (int i = 0; i < wb.getNumberOfSheets(); i++) {
				tempSheet = wb.getSheetAt(i);
				for (int j = 0; j <= tempSheet.getLastRowNum(); j++) {
					allSheetString.append(tempSheet.getRow(j).getCell(0) + sheetRowSplit);
				}
				allSheetString.append(sheetColumnSplit);
			}
			inp.close();
			wb.close();

			SXSSFWorkbook swb = new SXSSFWorkbook();
			SXSSFSheet sh = swb.createSheet("Sheet1");
			sh.setRandomAccessWindowSize(-1);
			String[] columnSheetStrings = allSheetString.toString().split(sheetColumnSplit);
			int tempRowNum = 0;
			Row tempRow;
			for (int i = 0; i < columnSheetStrings.length; i++) {
				tempRowNum = columnSheetStrings[i].split(sheetRowSplit).length;
				for (int j = 0; j < tempRowNum; j++) {
					if(sh.getRow(j) == null) {
						sh.createRow(j).createCell(i).setCellValue(columnSheetStrings[i].split(sheetRowSplit)[j]);
					}else {
						sh.getRow(j).createCell(i).setCellValue(columnSheetStrings[i].split(sheetRowSplit)[j]);
					}
				}
			}

			FileOutputStream out = new FileOutputStream(newExcelFile);
			swb.write(out);
			out.close();
			swb.dispose();
			swb.close();
		} catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public static int[] getMergedCellRegionIndex(String excelFile, String sheetName, String cellContent){
		int[] cellRegionIndex = new int[4];
		try{
			InputStream inp = new FileInputStream(excelFile);
			Workbook wb = WorkbookFactory.create(inp);
			Sheet sheet = wb.getSheet(sheetName);
			int rows = sheet.getLastRowNum();
			int startRow = 0;
			int cols = sheet.getRow(0).getLastCellNum();
			DataFormatter dataFormatter = new DataFormatter();
			for (int i = startRow; i <= rows; i++){
				for (int j = 0; j <= cols; j++){
					if(dataFormatter.formatCellValue(sheet.getRow(i).getCell(j)).equals(cellContent)){
						for(CellRangeAddress region : sheet.getMergedRegions()){
							if(region.isInRange(sheet.getRow(i).getCell(j))){
								cellRegionIndex[0] = region.getFirstColumn();
								cellRegionIndex[1] = region.getLastColumn();
								cellRegionIndex[2] = region.getFirstRow();
								cellRegionIndex[3] = region.getLastRow();
							}
						}
					}
				}
			}
			inp.close();
			wb.close();
		} catch (IOException e){
			e.printStackTrace();
		}
		return cellRegionIndex;
	}
}
