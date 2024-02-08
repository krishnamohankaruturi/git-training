package com.ess.controller;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.taglibs.standard.lang.jstl.LessThanOperator;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ess.ESSfactory.ESSFactoryProducer;
import com.ess.ESSfactory.EmpFactory;
import com.ess.ESSfactory.GeneralFactory;
import com.ess.ESSfactory.LeaveFactory;
import com.ess.ESSfactory.MasterFactory;
import com.ess.customModel.AttendanceDaysReportDetails;
import com.ess.customModel.AttendanceHoursReportDetails;
import com.ess.customModel.AttendanceReportDetails;
import com.ess.customModel.AttendanceSummaryDetails;
import com.ess.customModel.EmployeeAttendanceDetails;
import com.ess.customModel.EmployeeDetails;
import com.ess.customModel.EmployeeLeaveDetails;
import com.ess.customModel.EmployeeLeavedetailsInfo;
import com.ess.customModel.EmployeePasswordChange;
import com.ess.customModel.EmployeeSearch;
import com.ess.customModel.employeeOdDetailsInfo;
import com.ess.model.Attendancedetails;
import com.ess.model.Employeemaster;
import com.ess.model.EngineeringService;
import com.ess.model.LeaveConfiguration;
import com.ess.model.Leavebalance;
import com.ess.model.Leavedetails;
import com.ess.model.oddetails;
import com.ess.service.EmployeeService;
import com.ess.service.GeneralServices;
import com.ess.service.HrmsService;
import com.ess.service.LeaveDetails;
import com.ess.service.MasterService;
import com.ess.utility.EssConstants;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/emplyee")
public class EmployeeMasterController<T> {

	@Autowired
	ESSFactoryProducer factory;
	
	
	

	JSONObject json = new JSONObject();

	private static final Logger LOGGER = LogManager.getLogger(EmployeeMasterController.class.getName());
	private static final String EXCEL_PATH = "D:/Reports/Manoj Kumar S.xls"; // D:\ESS\MERGE\08-23-2019
																				// NEW\ESS-Angular\src\assets\Reports
	private static final String Attendance_PATH = "/home/admin/apache-tomcat-7.0.82/webapps/ess/assets/AttendanceReports/attendanceReport.xlsx";

	
	@RequestMapping(value = "/getEmplyeeDetails", method = RequestMethod.GET)
	public @ResponseBody String getEmployeeDetails() {
		LOGGER.info(" Method getEmployeeDetails");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		return genfac.getGenFac().objectToJsonConvertor(genfac.getGenFac().getCurrentUserDetails()).toString();
	}

	@GET
	// @Path("get")
	@RequestMapping(value = "/get", method = RequestMethod.GET, params = { "fromDate", "toDate", "empId",
			"bySupervisor" })
	@Produces("application/vnd.ms-excel")
	public @ResponseBody List<String> getContactExcelFile(@RequestParam(value = "fromDate") String fromDate,
			@RequestParam(value = "toDate") String toDate, @RequestParam(value = "empId") String empId,
			@RequestParam(value = "bySupervisor") String bySupervisor) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		FileInputStream file = null;
		try {
			file = new FileInputStream(new File(EXCEL_PATH));
			short headingNo = 0;
			short rowNo = 2;
			short sheetRowNo = 2;
			String newFromDate = "";
			String newToDate = "";
			HSSFWorkbook workBook = (HSSFWorkbook) WorkbookFactory.create(file);
			List<AttendanceDaysReportDetails> ad = empfac.getEmpFac().getAttendanceDaysReport(fromDate, toDate, empId,
					bySupervisor);
			List<AttendanceHoursReportDetails> adhrsReport = empfac.getEmpFac().getAttendanceHrsReport(fromDate, toDate,
					empId, bySupervisor);
			HSSFSheet sheet = workBook.getSheetAt(0);
			HSSFSheet sheet1 = workBook.getSheetAt(1);
			// Date Format
			SimpleDateFormat source = new SimpleDateFormat("yyyy-mm-dd");
			SimpleDateFormat target = new SimpleDateFormat("dd-mm-yyyy");
			try {
				newFromDate = target.format(source.parse(fromDate));
				newToDate = target.format(source.parse(toDate));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			HSSFRow reportHeadingOne = sheet.createRow(headingNo);
			if (sheet.getRow(headingNo) != null) {
				reportHeadingOne.createCell(0).setCellValue(EssConstants.FROM_DATE);
				reportHeadingOne.createCell(1).setCellValue(newFromDate);
				reportHeadingOne.createCell(2).setCellValue(EssConstants.ATTENDANCEDAYSREPORT);
				reportHeadingOne.createCell(9).setCellValue(EssConstants.TO_DATE);
				reportHeadingOne.createCell(10).setCellValue(newToDate);
			}

			HSSFRow reportHeadingTwo = sheet1.createRow(headingNo);
			if (sheet.getRow(headingNo) != null) {
				reportHeadingTwo.createCell(0).setCellValue(EssConstants.FROM_DATE);
				reportHeadingTwo.createCell(1).setCellValue(newFromDate);
				reportHeadingTwo.createCell(2).setCellValue(EssConstants.ATTENDANCEHOURSREPORT);
				reportHeadingTwo.createCell(10).setCellValue(EssConstants.TO_DATE);
				reportHeadingTwo.createCell(11).setCellValue(newToDate);
			}
			int lengthOfNewJoineesList = 0;
			List<Leavebalance> newjoineeslist = empfac.getEmpFac().getNewJoiningReport(fromDate, toDate);
			lengthOfNewJoineesList = newjoineeslist.size()-1;

			for (AttendanceDaysReportDetails objects : ad) {
				boolean isNewjoinee=false;
				for(int j=0;j<=lengthOfNewJoineesList;j++) {
					if(objects.getEmpId().equals(newjoineeslist.get(j).getEmpId())) {
						isNewjoinee=true;
						break;
					}
				}
				if(!isNewjoinee) {
				HSSFRow writingRoeStartsFrom = sheet.createRow(rowNo);
				if (sheet.getRow(rowNo) != null) {
					double d = objects.getLeavesCount().doubleValue();
					double d1 = objects.getOdWorkingDays().doubleValue();
					double d3 = objects.getPresentDays();
					double d4 = objects.getPresentDaysplusLeaves().doubleValue();
					double d5 = objects.getShortFallOfDays().doubleValue();

					writingRoeStartsFrom.createCell(0).setCellValue(objects.getEmpId());
					writingRoeStartsFrom.createCell(1).setCellValue(objects.getEmployeeName());
					writingRoeStartsFrom.createCell(2).setCellValue(d);
					writingRoeStartsFrom.createCell(3).setCellValue(d1);
					writingRoeStartsFrom.createCell(5).setCellValue(d3);
					writingRoeStartsFrom.createCell(6).setCellValue(d4);
					writingRoeStartsFrom.createCell(7).setCellValue(d5);
					writingRoeStartsFrom.createCell(8).setCellValue(objects.getReportingPersonName());

					rowNo++;
				}
				}
				
			}
			for (AttendanceHoursReportDetails objects : adhrsReport) {
				boolean isNewjoinee=false;
				for(int j=0;j<=lengthOfNewJoineesList;j++) {
					if(objects.getEmpId().equals(newjoineeslist.get(j).getEmpId())) {
						isNewjoinee=true;
						break;
					}
				}
				if(!isNewjoinee) {
				HSSFRow writingRoeStartsFromSheetTwo = sheet1.createRow(sheetRowNo);
				if (sheet1.getRow(sheetRowNo) != null) {
					writingRoeStartsFromSheetTwo.createCell(0).setCellValue(objects.getEmpId());
					writingRoeStartsFromSheetTwo.createCell(1).setCellValue(objects.getEmpName());
					writingRoeStartsFromSheetTwo.createCell(2).setCellValue(objects.getLeaveDays());
					writingRoeStartsFromSheetTwo.createCell(3).setCellValue(objects.getOnsiteWorrkingHours());
					// writingRoeStartsFromSheetTwo.createCell(4).setCellValue(objects.getWorkFromHome());
					writingRoeStartsFromSheetTwo.createCell(5).setCellValue(objects.getTotalEssWokingHours());
					// writingRoeStartsFromSheetTwo.createCell(6).setCellValue(objects.getTotalBiometricWrkHours());
					writingRoeStartsFromSheetTwo.createCell(6).setCellValue(objects.getTotalEssShortfallHours());
					// writingRoeStartsFromSheetTwo.createCell(8).setCellValue(objects.getTotalBioShortfallHours());
					writingRoeStartsFromSheetTwo.createCell(7).setCellValue(objects.getActualWorkingHours());
					writingRoeStartsFromSheetTwo.createCell(8).setCellValue(objects.getEssShortFallOfDays());
					// writingRoeStartsFromSheetTwo.createCell(11).setCellValue(objects.getEssbioShortFallOfDays());
					writingRoeStartsFromSheetTwo.createCell(9).setCellValue(objects.getReportingPersonName());
					sheetRowNo++;
					
				}
				}
			}
			for(int j=0;j<=lengthOfNewJoineesList;j++) {
				String newJoineeEmpId =  newjoineeslist.get(j).getEmpId();
				Timestamp newJoineeJoiningDate=empfac.getEmpFac().getNewJoineeJoiningDate(newJoineeEmpId);
				Date dayFromDate = newJoineeJoiningDate;
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				//DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
				String dayFromDate2 = dateFormat.format(dayFromDate);
				List<AttendanceDaysReportDetails> adnew = empfac.getEmpFac().getAttendanceDaysReport(dayFromDate2, toDate, newJoineeEmpId,
						bySupervisor);
				List<AttendanceHoursReportDetails> adhrsReportnew = empfac.getEmpFac().getAttendanceHrsReport(dayFromDate2, toDate,
						newJoineeEmpId, bySupervisor);
				
				SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat dateFormat3 = new SimpleDateFormat("yyyy-mm-dd");
				
				String newdate1=dateFormat2.format(dayFromDate);
//				String newdate2 = dateFormat2.format(source.parse(toDate));
				System.out.println("********************************		"+newdate1);
//				System.out.println("********************************		"+newdate2);
				
//				String newdate3=dateFormat3.format(dayFromDate);
				String newdate4 = dateFormat3.format(source.parse(toDate));
//				System.out.println("********************************		"+newdate3);
				System.out.println("********************************		"+newdate4);
				if( (newdate1.compareTo(newdate4) ) <=0 ) {
				for (AttendanceDaysReportDetails objects : adnew) 
				{
				HSSFRow writingRoeStartsFrom = sheet.createRow(rowNo);
				if (sheet.getRow(rowNo) != null) {
					double d = objects.getLeavesCount().doubleValue();
					double d1 = objects.getOdWorkingDays().doubleValue();
					double d3 = objects.getPresentDays();
					double d4 = objects.getPresentDaysplusLeaves().doubleValue();
					double d5 = objects.getShortFallOfDays().doubleValue();

					writingRoeStartsFrom.createCell(0).setCellValue(objects.getEmpId());
					writingRoeStartsFrom.createCell(1).setCellValue(objects.getEmployeeName());
					writingRoeStartsFrom.createCell(2).setCellValue(d);
					writingRoeStartsFrom.createCell(3).setCellValue(d1);
					writingRoeStartsFrom.createCell(5).setCellValue(d3);
					writingRoeStartsFrom.createCell(6).setCellValue(d4);
					writingRoeStartsFrom.createCell(7).setCellValue(d5);
					writingRoeStartsFrom.createCell(8).setCellValue(objects.getReportingPersonName());
					rowNo++;
				}
				}
			
				
				
				for (AttendanceHoursReportDetails objects : adhrsReportnew) {
					
			
					HSSFRow writingRoeStartsFromSheetTwo = sheet1.createRow(sheetRowNo);
					if (sheet1.getRow(sheetRowNo) != null) {
						writingRoeStartsFromSheetTwo.createCell(0).setCellValue(objects.getEmpId());
						writingRoeStartsFromSheetTwo.createCell(1).setCellValue(objects.getEmpName());
						writingRoeStartsFromSheetTwo.createCell(2).setCellValue(objects.getLeaveDays());
						writingRoeStartsFromSheetTwo.createCell(3).setCellValue(objects.getOnsiteWorrkingHours());
						// writingRoeStartsFromSheetTwo.createCell(4).setCellValue(objects.getWorkFromHome());
						writingRoeStartsFromSheetTwo.createCell(5).setCellValue(objects.getTotalEssWokingHours());
						// writingRoeStartsFromSheetTwo.createCell(6).setCellValue(objects.getTotalBiometricWrkHours());
						writingRoeStartsFromSheetTwo.createCell(6).setCellValue(objects.getTotalEssShortfallHours());
						// writingRoeStartsFromSheetTwo.createCell(8).setCellValue(objects.getTotalBioShortfallHours());
						writingRoeStartsFromSheetTwo.createCell(7).setCellValue(objects.getActualWorkingHours());
						writingRoeStartsFromSheetTwo.createCell(8).setCellValue(objects.getEssShortFallOfDays());
						// writingRoeStartsFromSheetTwo.createCell(11).setCellValue(objects.getEssbioShortFallOfDays());
						writingRoeStartsFromSheetTwo.createCell(9).setCellValue(objects.getReportingPersonName());
						sheetRowNo++;
						
					}
					
				}
				}
				else {
					break;
				}
				
			}
			file.close();
			// Open FileOutputStream to write updates //D:\ESS\MERGE\08-23-2019
			// NEW\ESS-Angular\src\assets\Reports
			File output_file = new File("E:\\QA code new version 4.0\\Angular\\src\\assets\\Reports\\Report.xls");
			workBook.write(output_file);
			ResponseBuilder response = Response.ok((Object) output_file);
			List<String> responselist=new ArrayList<>();
			response.header("Content-Disposition", "attachment; filename=\"contact_excel_file.xls\"");
			responselist.add(response.build().toString());
			System.out.println(responselist);
			return responselist;

		} catch (Exception e) {

			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/getEmplyeeById/123
	 *               here "123" is the value provided by you this method will retun
	 *               the emplyee details as json string
	 */
	@RequestMapping(value = "/getEmplyeeDetailsById/{empid}", method = RequestMethod.GET)
	public @ResponseBody String getEmployeeById(@PathVariable String empid) {
		LOGGER.info(" Method getEmployeeById");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		return genfac.getGenFac().objectToJsonConvertor(empfac.getEmpFac().getEmployeeById(empid)).toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/getAllEmployee/123
	 *               here "123" is the value provided by you this method will retun
	 *               the list of emplyee details as json string
	 */
	@RequestMapping(value = "/getAllEmployeeDetails", method = RequestMethod.GET)
	public @ResponseBody String getAllEmployee() {
		EmpFactory empfac = this.factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = this.factory.getFactory("GeneralFactory", "GeneralServiceImpl");

		if (EssConstants.USER_ROLE_ADMIN.equals(genfac.getGenFac().getCurrentUserDetails().getRole())) {
			return genfac.getGenFac().objectArrayToJsonConvertor(empfac.getEmpFac().getAllEmployees()).toString();
		} else {
			return genfac.getGenFac()
					.objectArrayToJsonConvertor(
							empfac.getEmpFac().getEmployeesReportingPerson(genfac.getGenFac().getCurrentUserId()))
					.toString();
		}
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/getAllEmployee/123
	 *               here "123" is the value provided by you this method will retun
	 *               the list of emplyee details as json string
	 */
	@RequestMapping(value = "/getAllEmployeeDetails", method = RequestMethod.POST)
	public @ResponseBody String getAllEmployee(@RequestBody EmployeeDetails empDet) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		EmployeeDetails curEmpDet = genfac.getGenFac().getCurrentUserDetails();
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(
						empfac.getEmpFac().getAllEmployees(empDet, curEmpDet.getEmpId(), curEmpDet.getRole()))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/getEmployeesByRP/123
	 *               here "123" is the value provided by you this method will retun
	 *               the list of emplyee details as json string
	 */
	@RequestMapping(value = "/getEmployeesForRP", method = RequestMethod.GET)
	public @ResponseBody String getEmployeeReportingPerson() {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(
						empfac.getEmpFac().getEmployeesReportingPerson(genfac.getGenFac().getCurrentUserId()))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/getEmployeesByRP/123
	 *               here "123" is the value provided by you this method will retun
	 *               the list of emplyee details as json string
	 */
	@RequestMapping(value = "/employeeSearch", method = RequestMethod.GET)
	public @ResponseBody String employeeSearch() {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		MasterFactory masfac = factory.getFactory("MasterFactory", "MasterServiceImpl");
		EmployeeSearch empSearch = new EmployeeSearch();
		empSearch.setDesignation(masfac.getMasterFac().getAllMastersByType(EssConstants.MASTER_TYPE_DESG));
		empSearch.setProjects(masfac.getMasterFac().getAllMastersByType(EssConstants.MASTER_TYPE_PROJ));
		empSearch.setCalendar(masfac.getMasterFac().getAllMastersByType(EssConstants.MASTER_TYPE_CALENDAR));
		empSearch.setEmployeeTypeId(masfac.getMasterFac().getAllMastersByType(EssConstants.MASTER_TYPE_EMP_TYPE));
		empSearch.setEmpDet(empfac.getEmpFac().getEmployeesupervisors(EssConstants.USER_ROLE_SUPERVISOR));
		empSearch.setBloodGroupId(masfac.getMasterFac().getEmployeeByBloodGroup(EssConstants.MASTER_TYPE_BLOODGROUP));
		return genfac.getGenFac().objectToJsonConvertor(empSearch).toString();
	}

	/**
	 * .
	 * 
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/getEmployeesByRP/123
	 *               here "123" is the value provided by you this method will retun
	 *               the list of emplyee details as json string
	 */
	@RequestMapping(value = "/employeeSave", method = RequestMethod.POST)
	public @ResponseBody String employeeSave(@RequestBody EmployeeDetails empDet) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		String Flag = empfac.getEmpFac().saveEmployeeMaster(empDet);
		return Flag;
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/leaveSettingSave/123
	 *               here "123" is the value provided by you this method will post
	 *               the list details as json string
	 */
	@RequestMapping(value = "/leaveSettingSave", method = RequestMethod.POST)
	public @ResponseBody List<String> leaveSettingSave(@RequestBody LeaveConfiguration leaveSet) {
		LOGGER.info("Inside Leave Setting Controller");
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		List<String> obj = new ArrayList<String>();
		obj.add(genfac.getGenFac().objectToJsonConvertor(empfac.getEmpFac().getLeaveSettingEmployeeByType(leaveSet))
				.toString());
		return obj;
	}

	/**
	 * suppose you want to call an api by providing a value say
	 * http://localhost:8080/ESS/api/emplyee/validateOauthToken here "123" is the
	 * value provided by you this method will retun the json string Valid
	 * accessToken
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/validateOauthToken", method = RequestMethod.GET)
	public @ResponseBody String validateOauthToken() {
		json.put(" message ", "Valid accessToken");
		return json.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/getAllEmployee/123
	 *               here "123" is the value provided by you this method will retun
	 *               the Levae details of the user
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getLeaveDetails/{empId}", method = RequestMethod.GET)
	public @ResponseBody String getLeaveDetailsById(@PathVariable String empId) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		json.put("employeeDetails", empfac.getEmpFac().getAllEmployees());
		return json.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/getEmployeesByRP/123
	 *               here "123" is the value provided by you
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getCompaOffDetails/{empId}", method = RequestMethod.GET)
	public @ResponseBody String getLeaveDetailsByRP(@PathVariable String reportingPerson) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		json.put("employeeDetails", empfac.getEmpFac().getEmployeeById(reportingPerson));
		return json.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/dashboard/123 here
	 *               "123" is the value provided by you is an employeeId
	 */
	@RequestMapping(value = "/dashboard/{empId}", method = RequestMethod.GET)
	public @ResponseBody String getDashBoard(@PathVariable String empId) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		return genfac.getGenFac().objectToJsonConvertor(empfac.getEmpFac().getEmployeeDashBoardDetailsById(empId))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say
	 *               http://localhost:8080/ESS/api/emplyee/leaveConfigurationDetails/123
	 *               here "123" is the value provided by you is an employeeId
	 */

	@RequestMapping(value = "/leaveConfigurationDetails", method = RequestMethod.GET)
	public @ResponseBody String getLeaveConfigurationDetails() {
		LOGGER.info("INSIDE EMPLOYEEMASTER CONTROLLER LEAVECONFIGURATIONDETAILS METHOD ");
		// LOGGER.info("gdfgdfgdfg"+empobj.getEmployeeLeaveConfigurationDetails());
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		return genfac.getGenFac().objectArrayToJsonConvertor(empfac.getEmpFac().getEmployeeLeaveConfigurationDetails())
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/getEmployeeLeaves/
	 * 
	 */
	@RequestMapping(value = "/employeeLeaves", method = RequestMethod.GET)
	public @ResponseBody String getEmployeeLeaves() {
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");

		return genfac.getGenFac().objectArrayToJsonConvertor(leavefac.getLeaveFac().getAllEmployees()).toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/employeeLeavesForRP
	 *
	 *               this method will retun the list of emplyee details as json
	 *               string
	 */
	@RequestMapping(value = "/employeeLeavesForRP", method = RequestMethod.GET)
	public @ResponseBody String getEmployeeLeavesReportingPerson() {
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(
						leavefac.getLeaveFac().getEmployeesReportingPerson(genfac.getGenFac().getCurrentUserId()))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/applyLeave/
	 * 
	 */
	/**
	 * @SuppressWarnings("unchecked")
	 * 
	 * @RequestMapping(value="/applyLeave", method = RequestMethod.POST)
	 *                                      public @ResponseBody String
	 *                                      applyLeave(@RequestBody Leavedetails
	 *                                      empLeaveDet) {
	 *                                      System.out.println("Inside----
	 *                                      "+empLeaveDet);
	 *                                      System.out.println("Inside----
	 *                                      "+empLeaveDet.getEmployeemaster());
	 *                                      return
	 *                                      genobj.objectArrayToJsonConvertor(leaveDetails.saveEmployeeLeaves(empLeaveDet)).toString();
	 *                                      } *
	 */

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/applyLeave1/
	 * 
	 */
	@RequestMapping(value = "/applyLeave", method = RequestMethod.POST)
	public @ResponseBody String applyLeave(@RequestBody EmployeeLeavedetailsInfo empLeaveDet) {
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");
		return genfac.getGenFac().objectArrayToJsonConvertor(leavefac.getLeaveFac().saveEmployeeLeaves(empLeaveDet))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/employeeNames/123
	 *               here "123" is the value provided by you is an employeeId
	 */
	@RequestMapping(value = "/employeeNames", method = RequestMethod.GET)
	public @ResponseBody String getEmployeeNamesBasedOnRP() {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		String role = genfac.getGenFac().getCurrentUserDetails().getRole();
		if (!role.equals(EssConstants.USER_ROLE_EMPLOYEE) || !role.equals(EssConstants.USER_ROLE_TRAINEE)) {
			return genfac.getGenFac().objectArrayToJsonConvertor(
					empfac.getEmpFac().getEmployeeNameByRp(genfac.getGenFac().getCurrentUserId())).toString();
		}
		return null;

	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/leaveHistorySearch/
	 *               here "123" is the value provided by you is an employeeId
	 */
	@RequestMapping(value = "/leaveHistorySearch", method = RequestMethod.POST)
	public @ResponseBody String leaveHistorySearch(@RequestBody EmployeeLeavedetailsInfo empLeaveDet) {
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");
		String role = genfac.getGenFac().getCurrentUserDetails().getRole();
		if (role.equals(EssConstants.USER_ROLE_EMPLOYEE) || role.equals(EssConstants.USER_ROLE_TRAINEE)) {
			empLeaveDet.setEmpIdIn(genfac.getGenFac().getCurrentUserId());
		}
		return genfac.getGenFac().objectArrayToJsonConvertor(leavefac.getLeaveFac().leaveHistorySearch(empLeaveDet))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say
	 *               http://localhost:8080/ESS/api/emplyee/attendanceDetailsByRole/
	 *               here "123" is the value provided by you is an employeeId
	 */
	@RequestMapping(value = "/attendanceDetailsByRole", method = RequestMethod.GET)
	public @ResponseBody String attendanceDetailsByRole() {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(
						empfac.getEmpFac().getAttendanceDetailsByRole(genfac.getGenFac().getCurrentUserId()))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say
	 *               http://localhost:8080/ESS/api/emplyee/empAttendanceDetailsSearch/
	 *               here "123" is the value provided by you is an employeeId
	 */
	@RequestMapping(value = "/empAttendanceDetailsSearch", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody String empAttendanceDetails(@RequestBody EmployeeAttendanceDetails empAttendanceDetails) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		if (genfac.getGenFac().getCurrentUserDetails().getRole().equals(EssConstants.USER_ROLE_EMPLOYEE)
				|| genfac.getGenFac().getCurrentUserDetails().getRole().equals(EssConstants.USER_ROLE_TRAINEE)) {
			empAttendanceDetails.setEmpId(genfac.getGenFac().getCurrentUserId());
		}
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(empfac.getEmpFac().getAttendanceDetailsSearch(empAttendanceDetails))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say
	 *               http://localhost:8080/ESS/api/emplyee/attendanceSummaryByRole/
	 */
	@RequestMapping(value = "/attendanceSummaryByRole", method = RequestMethod.GET)
	public @ResponseBody String attendanceSummary() {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(
						empfac.getEmpFac().getAttendanceSummaryByRole(genfac.getGenFac().getCurrentUserId()))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say
	 *               http://localhost:8080/ESS/api/emplyee/empAttendanceSummarySearch/
	 */
	@RequestMapping(value = "/empAttendanceSummarySearch", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody String searchAttendanceSummaryDetails(
			@RequestBody AttendanceSummaryDetails attendanceSummaryDetails) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		if (genfac.getGenFac().getCurrentUserDetails().getRole().equals(EssConstants.USER_ROLE_EMPLOYEE)
				|| genfac.getGenFac().getCurrentUserDetails().getRole().equals(EssConstants.USER_ROLE_TRAINEE)) {
			attendanceSummaryDetails.setEmpId(genfac.getGenFac().getCurrentUserId());
		}
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(empfac.getEmpFac().getAttendanceSummarySearch(attendanceSummaryDetails))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/getUserDetails/
	 * 
	 */
	@RequestMapping(value = "/getUserDetails", method = RequestMethod.POST)
	public @ResponseBody List<String> getUserDetails(@RequestBody EmployeePasswordChange empPasswordDet) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		boolean flag = empfac.getEmpFac().changePassword(empPasswordDet);
		List<String> obj = new ArrayList<String>();
		String response;
		if (flag == true) {
			response = "Success";
		} else {
			response = "Failure";
		}
		obj.add(response);
		return obj;
	}

	/**
	 * 
	 */
	@RequestMapping(value = "/empAttendaceAuthorization", method = RequestMethod.POST)
	public @ResponseBody String empAttendaceAuthorization(@RequestBody List<Attendancedetails> empAuthorizationEdit) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		empfac.getEmpFac().attendanceAuthorization(empAuthorizationEdit);
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(
						empfac.getEmpFac().getAttendanceDetailsByRole(genfac.getGenFac().getCurrentUserId()))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/getEmployeesByRP/123
	 *               here "123" is the value provided by you this method will retun
	 *               the list of emplyee details as json string
	 */
	@RequestMapping(value = "/updateEmployeeDetails", method = RequestMethod.POST)
	public String updateEmployeeDetails(@RequestBody EmployeeDetails empDet) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		return genfac.getGenFac().objectToJsonConvertor(empfac.getEmpFac().updateEmployeeMaster(empDet)).toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/getAllEmployee/123
	 *               here "123" is the value provided by you this method will retun
	 *               the list of emplyee details as json string
	 */
	@RequestMapping(value = "/getEmployeeLeaveDetails", method = RequestMethod.GET)
	public @ResponseBody String getEmployeeLeaveDetailsList() {
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(
						leavefac.getLeaveFac().getEmployeeLeaveDetails(genfac.getGenFac().getCurrentUserId()))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/cancelLeave/
	 * 
	 */
	@RequestMapping(value = "/cancelLeave", method = RequestMethod.POST)
	public @ResponseBody String cancelLeave(@RequestBody List<EmployeeLeavedetailsInfo> empLeaveDetList) {
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(leavefac.getLeaveFac().cancelEmployeeLeaves(empLeaveDetList)).toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/leaveHistorySearch/
	 *               here "123" is the value provided by you is an employeeId
	 */
	@RequestMapping(value = "/leaveCancelSearch", method = RequestMethod.POST)
	public @ResponseBody String leaveCancelSearch(@RequestBody EmployeeLeavedetailsInfo empLeaveDet) {
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");
		empLeaveDet.setEmpIdIn(genfac.getGenFac().getCurrentUserId());
		return genfac.getGenFac().objectArrayToJsonConvertor(leavefac.getLeaveFac().leaveCancelSearch(empLeaveDet))
				.toString();
	}
	
	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say
	 *               http://localhost:8080/ESS/api/emplyee/isLeaveAlreadyApplied/
	 *               here "123" is the value provided by you is an employeeId
	 */
	@RequestMapping(value = "/isLeaveAlreadyApplied", method = RequestMethod.POST)
	public @ResponseBody List<String> isLeaveAlreadyApplied(@RequestBody EmployeeLeavedetailsInfo empAppliedLeaveDet) {
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");
		List<String> obj = new ArrayList<String>();
		empAppliedLeaveDet.setEmpIdIn(genfac.getGenFac().getCurrentUserId());
		boolean flag = leavefac.getLeaveFac().isLeaveAlreadyApplied(empAppliedLeaveDet);
		String response = null;
		if (flag == true) {
			response = "Success";
		} else {
			response = "Failure";
		}
		obj.add(response);
		return obj;
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say
	 *               http://localhost:8080/ESS/api/emplyee/employeeLeaveDetailsForAuth
	 *
	 *               this method will return the list of employee leave details as
	 *               json string
	 */
	@RequestMapping(value = "/getEmployeeLeaveDetailsForAuth", method = RequestMethod.GET)
	public @ResponseBody String getEmployeeLeaveDetailsForAuth() {
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");
		return genfac.getGenFac().objectArrayToJsonConvertor(
				leavefac.getLeaveFac().getAllEmployeesLeaveDetailsForAuth(genfac.getGenFac().getCurrentUserId()))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/authoriseLeave/
	 * 
	 */
	@RequestMapping(value = "/authoriseLeave", method = RequestMethod.POST)
	public @ResponseBody String authoriseLeave(@RequestBody List<EmployeeLeavedetailsInfo> empLeaveDetList) {
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(leavefac.getLeaveFac().authoriseEmployeeLeaves(empLeaveDetList)).toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/rejectLeave/
	 * 
	 */
	@RequestMapping(value = "/rejectLeave", method = RequestMethod.POST)
	public @ResponseBody String rejectLeave(@RequestBody List<EmployeeLeavedetailsInfo> empLeaveDetList) {
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(leavefac.getLeaveFac().rejectEmployeeLeaves(empLeaveDetList)).toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say
	 *               http://localhost:8080/ESS/api/emplyee/leaveAuthorisationSearch/
	 *               here "123" is the value provided by you is an employeeId
	 */
	@RequestMapping(value = "/leaveAuthorisationSearch", method = RequestMethod.POST)
	public @ResponseBody String leaveAuthorisationSearch(@RequestBody EmployeeLeavedetailsInfo empLeaveDet) {
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(leavefac.getLeaveFac().getLeaveAuthSearchDetails(empLeaveDet)).toString();
	}

	/**
	 * 
	 * @param attendanceDetails
	 * @return
	 */
	@RequestMapping(value = "/addOnsiteDetails", method = RequestMethod.POST)
	public @ResponseBody String addOnsiteDetails(@RequestBody Attendancedetails attendanceDetails) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		empfac.getEmpFac().addOnsiteRequest(attendanceDetails);
		return null;
	}

	/**
	 * URL of the method
	 * http://localhost:8080/ESS/api/api/emplyee/getAllOnsiteDetails Have no path
	 * variable Return the list of onsite
	 */

	@RequestMapping(value = "/getAllOnsiteDetails", method = RequestMethod.GET)
	public @ResponseBody String getOnsiteList() {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(empfac.getEmpFac().getAllOnsiteList(genfac.getGenFac().getCurrentUserId()))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say
	 *               http://localhost:8080/ESS/api/emplyee/getOnsiteDetailsById/123
	 *               here "123" is the value provided by you this method will retun
	 *               the emplyee details as json string
	 */
	@RequestMapping(value = "/getOnsiteDetailsById/{transId}", method = RequestMethod.GET)
	public @ResponseBody String getOnsiteDetailsById(@PathVariable Long transId) {
		LOGGER.info(" Method getEmployeeById");
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		return genfac.getGenFac().objectToJsonConvertor(empfac.getEmpFac().getOnsiteListById(transId)).toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/resetPassword/
	 * 
	 */
	@RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
	public @ResponseBody List<String> resetPassword(@RequestBody EmployeePasswordChange empPasswordDet) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		boolean flag = empfac.getEmpFac().resetPassword(empPasswordDet);
		List<String> obj = new ArrayList<String>();
		String response;
		if (flag == true) {
			response = "Success";
		} else {
			response = "Failure";
		}
		obj.add(response);
		return obj;
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/OnsiteDetailsSearch/
	 *               here "123" is the value provided by you is an employeeId
	 */
	@RequestMapping(value = "/OnsiteDetailsSearch", method = RequestMethod.POST)
	public @ResponseBody String OnsiteDetailsSearch(@RequestBody EmployeeAttendanceDetails onsiteDet) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		return genfac.getGenFac().objectArrayToJsonConvertor(empfac.getEmpFac().getOnsiteSearchDetails(onsiteDet))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say
	 *               http://localhost:8080/ESS/api/emplyee/attendanceAuthorizationSearch/
	 */
	@RequestMapping(value = "/attendanceAuthorizationSearch", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody String searchAttendanceAuthorzation(
			@RequestBody EmployeeAttendanceDetails attendanceAuthorizationsearch) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		return genfac.getGenFac().objectArrayToJsonConvertor(
				empfac.getEmpFac().getAttendanceDetailsBySearch(attendanceAuthorizationsearch)).toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/getEmpHolidayDates/
	 *               here "123" is the value provided by you is an employeeId
	 */
	@RequestMapping(value = "/getEmpHolidayDates", method = RequestMethod.GET)
	public @ResponseBody String getEmpHolidayDates() {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		return genfac.getGenFac().objectArrayToJsonConvertor(
				empfac.getEmpFac().getEmployeeHolidayDates(genfac.getGenFac().getCurrentUserId())).toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/getEmployeesByRP/123
	 *               here "123" is the value provided by you this method will retun
	 *               the list of emplyee details as json string
	 */
	@RequestMapping(value = "/updateOnsiteDetails", method = RequestMethod.POST)
	public @ResponseBody String updateOnsiteDetails(@RequestBody Attendancedetails onsiteEdit) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		empfac.getEmpFac().updateOnsiteMaster(onsiteEdit);
		return null;
	}

	/**
	 * 
	 * @param attendanceDetails
	 * @return
	 */
	@RequestMapping(value = "/addWorkFromHomeDetails", method = RequestMethod.POST)
	public @ResponseBody String addWorkFromHomeDetails(@RequestBody Attendancedetails attendanceDetails) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		empfac.getEmpFac().addWorkFromHomeRequest(attendanceDetails);
		return null;
	}

	/**
	 * URL of the method
	 * http://localhost:8080/ESS/api/api/emplyee/getAllWorkFromHomeDetails Have no
	 * path variable Return the list of workfromhome
	 */

	@RequestMapping(value = "/getAllWorkFromHomeDetails", method = RequestMethod.GET)
	public @ResponseBody String getWorkFromHomeList() {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		return genfac.getGenFac().objectArrayToJsonConvertor(
				empfac.getEmpFac().getAllWorkFromHomeList(genfac.getGenFac().getCurrentUserId())).toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say
	 *               http://localhost:8080/ESS/api/emplyee/getWorkfromhomeDetailsById/123
	 *               here "123" is the value provided by you this method will retun
	 *               the emplyee details as json string
	 */
	@RequestMapping(value = "/getWorkfromhomeDetailsById/{transId}", method = RequestMethod.GET)
	public @ResponseBody String getWorkfromhomeDetailsById(@PathVariable Long transId) {
		LOGGER.info(" Method getEmployeeById");
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		return genfac.getGenFac().objectToJsonConvertor(empfac.getEmpFac().getWorkfromhomeListById(transId)).toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say
	 *               http://localhost:8080/ESS/api/emplyee/WorkfromhomeDetailsSearch/
	 *               here "123" is the value provided by you is an employeeId
	 */
	@RequestMapping(value = "/WorkfromhomeDetailsSearch", method = RequestMethod.POST)
	public @ResponseBody String WorkfromhomeDetailsSearch(@RequestBody EmployeeAttendanceDetails workfromhomeDet) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(empfac.getEmpFac().getWorkfromhomeSearchDetails(workfromhomeDet))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say
	 *               http://localhost:8080/ESS/api/emplyee/updateWorkfromhomeDetails
	 *               this method will retun the list of emplyee details as json
	 *               string
	 */
	@RequestMapping(value = "/updateWorkfromhomeDetails", method = RequestMethod.POST)
	public @ResponseBody String updateWorkfromhomeDetails(@RequestBody Attendancedetails workfromhomeEdit) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		empfac.getEmpFac().updateWorkfromhomeMaster(workfromhomeEdit);
		return null;
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/getBySupervisorList/
	 *               here "123" is the value provided by you is an employeeId
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getBySupervisorList", method = RequestMethod.GET)
	public @ResponseBody String getBySupervisorList() {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(
						empfac.getEmpFac().getBySupervisorListDetails(genfac.getGenFac().getCurrentUserId()))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/getEmployeeRole/ this
	 *               method will retun the list of employee role as json string
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getEmployeeRole", method = RequestMethod.GET)
	public @ResponseBody String getEmployeeRole() {
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		return genfac.getGenFac().getCurrentUserDetails().getRole();
	}

	@RequestMapping(value = "/getEmployeeBirthdayDetails", method = RequestMethod.GET)
	public @ResponseBody String getEmployeeBirthdayDetails() {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		return genfac.getGenFac().objectArrayToJsonConvertor(empfac.getEmpFac().getAllEmployees()).toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say
	 *               http://localhost:8080/ESS/api/emplyee/getLeaveBalanceDetails
	 *
	 *               this method will return the list of employee leave balance
	 *               details as json string
	 */
	@RequestMapping(value = "/getLeaveBalanceDetails", method = RequestMethod.GET)
	public @ResponseBody String getLeaveBalanceDetails() {
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(
						leavefac.getLeaveFac().getEmpLeaveBalanceDetails(genfac.getGenFac().getCurrentUserId()))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/saveLeaveBalance/
	 * 
	 */
	@RequestMapping(value = "/saveLeaveBalance", method = RequestMethod.POST)
	public @ResponseBody String saveLeaveBalance(@RequestBody List<EmployeeLeaveDetails> empLeaveBalDetList) {
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(leavefac.getLeaveFac().saveEmployeeLeaveBalance(empLeaveBalDetList))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/leaveBalanceSearch/
	 */
	@RequestMapping(value = "/leaveBalanceSearch", method = RequestMethod.POST)
	public @ResponseBody String leaveBalanceSearch(@RequestBody EmployeeLeaveDetails empLeaveBalDet) {
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(leavefac.getLeaveFac().getLeaveBalanceSearchDetails(empLeaveBalDet))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say
	 *               http://localhost:8080/ESS/api/emplyee/attendanceDetailsByRole/
	 *               here "123" is the value provided by you is an employeeId
	 */
	@RequestMapping(value = "/attendanceDetailsByRoleForReport", method = RequestMethod.GET)
	public @ResponseBody String attendanceDetailsByRoleForReport() {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(
						empfac.getEmpFac().getAttendanceDetailsByRoleForReport(genfac.getGenFac().getCurrentUserId()))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say
	 *               http://localhost:8080/ESS/api/emplyee/empAttendanceDetailsSearch/
	 *               here "123" is the value provided by you is an employeeId
	 */
	@RequestMapping(value = "/empAttendanceReportDetailsSearch", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody String empAttendanceReportDetailsSearch(
			@RequestBody EmployeeAttendanceDetails empAttendanceDetails) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		if (genfac.getGenFac().getCurrentUserDetails().getRole().equals(EssConstants.USER_ROLE_EMPLOYEE)
				|| genfac.getGenFac().getCurrentUserDetails().getRole().equals(EssConstants.USER_ROLE_TRAINEE)) {
			empAttendanceDetails.setEmpId(genfac.getGenFac().getCurrentUserId());
		}
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(empfac.getEmpFac().getAttendanceReportDetailsSearch(empAttendanceDetails))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say
	 *               http://localhost:8080/ESS/api/emplyee/employeeMonthlyReport/
	 */
	@RequestMapping(value = "/employeeMonthlyReport", params = { "month", "year" })
	public @ResponseBody String employeeReport(@RequestParam(value = "month") String month,
			@RequestParam(value = "year") String year) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		return genfac.getGenFac().objectArrayToJsonConvertor(empfac.getEmpFac().getEmployeeMonthlyreport(year, month))
				.toString();

	}

	/**
	 * 
	 * for returning total employeemastercount
	 * 
	 * @return
	 */
	@RequestMapping(value = "/employeeCount", method = RequestMethod.GET)
	public @ResponseBody long employeeCount() {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		long countOfEmployee = empfac.getEmpFac().employeeCount();
		return countOfEmployee;
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/leaveSettingSave/123
	 *               here "123" is the value provided by you this method will post
	 *               the list details as json string
	 */
	@RequestMapping(value = "/fileupload", method = RequestMethod.POST)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public @ResponseBody String fileuploadSave(@RequestParam(value = "upfile[]", required = false) MultipartFile file)
			throws Exception {
		// LOGGER.info(" Leave Policy upload service ");
		String realPathtoUploads = "/usr/share/nginx/html/assets/LeavePolicy/";
		LOGGER.info(" Leave Policy upload Path info ", realPathtoUploads);
		if (!new File(realPathtoUploads).exists()) {
			new File(realPathtoUploads).mkdir();
		}
		String orgName = "LeavePolicy.doc";
		String filePath = realPathtoUploads + orgName;
		File dest = new File(filePath);
		LOGGER.info(" Leave Policy dest ", dest);
		file.transferTo(dest);
		orgName = "";
		return "Success";

	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/leaveSettingSave/123
	 *               here "123" is the value provided by you this method will post
	 *               the list details as json string
	 */
	@RequestMapping(value = "/Attendancedataupload", method = RequestMethod.POST)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public @ResponseBody String Attendancedataupload(
			@RequestParam(value = "upfile[]", required = false) MultipartFile file) throws Exception {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");

		String realPathtoUploads = "D://AttendanceReports//";
		if (!new File(realPathtoUploads).exists()) {
			new File(realPathtoUploads).mkdir();
		}
		String orgName = "attendanceReport.xlsx";
		String filePath = realPathtoUploads + orgName;
		File dest = new File(filePath);
		file.transferTo(dest);
		orgName = "";

		FileInputStream file2 = null;
		try {
			file2 = new FileInputStream(new File(Attendance_PATH));
			XSSFWorkbook workbook = new XSSFWorkbook(file2);
			List<Attendancedetails> empAttendanceDetailsList2 = new ArrayList<Attendancedetails>();
			// Get first/desired sheet from the workbook
			XSSFSheet sheet = workbook.getSheetAt(0);
			List<Integer> empidList = new ArrayList<Integer>();
			int rowcount = sheet.getPhysicalNumberOfRows();
			int colcount = sheet.getRow(0).getPhysicalNumberOfCells();
			Date updatedDate = (Date) sheet.getRow(0).getCell(1).getDateCellValue();
			for (int i = 2; i < rowcount; i++) {
				XSSFRow row = sheet.getRow(i);
				Integer empId1 = (int) sheet.getRow(i).getCell(0).getNumericCellValue();
				if (empId1 != 0) {
					String empId = Integer.toString(empId1);
					Date TimeIn = sheet.getRow(i).getCell(1).getDateCellValue();
					Date TimeOut = sheet.getRow(i).getCell(2).getDateCellValue();
					Attendancedetails empUpdate2 = new Attendancedetails(empId, TimeIn, TimeOut);
					empAttendanceDetailsList2.add(empUpdate2);
				}

			}
			String s = genfac.getGenFac().objectArrayToJsonConvertor(
					empfac.getEmpFac().updatingEmployeeAttendanceDetails(empAttendanceDetailsList2, updatedDate))
					.toString();

		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * 
	 * @param Add odDetails
	 * @return
	 */
	@RequestMapping(value = "/addOdDetails", method = RequestMethod.POST)
	public @ResponseBody List<String> odDetails(@RequestBody oddetails odRequestDetails) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		List<String> obj = new ArrayList<String>();
		obj.add(empfac.getEmpFac().addOdRequest(odRequestDetails).toString());
		return obj;

	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say
	 *               http://localhost:8080/ESS/api/emplyee/employeeOdRequestDetails
	 *
	 *               this method will retun the list of emplyee details as json
	 *               string
	 */
	@RequestMapping(value = "/employeeOdForRP", method = RequestMethod.GET)
	public @ResponseBody String getEmployeeOdReportingPerson() {
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(
						leavefac.getLeaveFac().getEmployeesOdReportingPerson(genfac.getGenFac().getCurrentUserId()))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say
	 *               http://localhost:8080/ESS/api/emplyee/odDetailsAuthorization
	 *
	 *               this method will retun the list of emplyee details as json
	 *               string
	 */
	@RequestMapping(value = "/getEmployeeOdDetailsForAuth", method = RequestMethod.GET)
	public @ResponseBody String getEmployeeOdDetailsForAuth() {
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(
						leavefac.getLeaveFac().getAllEmployeesOdDetailsForAuth(genfac.getGenFac().getCurrentUserId()))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/authoriseOd/
	 * 
	 */
	@RequestMapping(value = "/authoriseOd", method = RequestMethod.POST)
	public @ResponseBody String authoriseOd(@RequestBody List<oddetails> empLeaveDetList) {
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(leavefac.getLeaveFac().authoriseEmployeeOd(empLeaveDetList)).toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/rejectOd/
	 * 
	 */
	@RequestMapping(value = "/rejectOd", method = RequestMethod.POST)
	public @ResponseBody String rejectOd(@RequestBody List<oddetails> empOdDetList) {
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");
		return genfac.getGenFac().objectArrayToJsonConvertor(leavefac.getLeaveFac().rejectEmployeeOd(empOdDetList))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/leaveHistorySearch/
	 *               here "123" is the value provided by you is an employeeId
	 */
//	@RequestMapping(value="/OdDetailsSearch", method = RequestMethod.POST)
//	public @ResponseBody String OdDetailsSearch(@RequestBody employeeOdDetailsInfo empODDet) {
//		String role=genobj.getCurrentUserDetails().getRole();
//		System.out.println("inside oddetails seach controller");
//		 if(role.equals(EssConstants.USER_ROLE_EMPLOYEE) || role.equals(EssConstants.USER_ROLE_TRAINEE)){
//			 empODDet.setEmpIdIn(genobj.getCurrentUserId());
//		 }
//		return genobj.objectArrayToJsonConvertor(leaveDetails.OdDetailsSearch(empODDet)).toString();
//	}

	@RequestMapping(value = "/odHistorySearch", method = RequestMethod.POST)
	public @ResponseBody String leaveHistorySearcha(@RequestBody EmployeeLeavedetailsInfo empLeaveDet) {
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");
		String role = genfac.getGenFac().getCurrentUserDetails().getRole();
		if (role.equals(EssConstants.USER_ROLE_EMPLOYEE) || role.equals(EssConstants.USER_ROLE_TRAINEE)) {
			empLeaveDet.setEmpIdIn(genfac.getGenFac().getCurrentUserId());
		}
		return genfac.getGenFac().objectArrayToJsonConvertor(leavefac.getLeaveFac().OdDetailsSearch(empLeaveDet))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say
	 *               http://localhost:8080/ESS/api/emplyee/OdAuthorisationSearch/
	 *               here "123" is the value provided by you is an employeeId
	 */
	@RequestMapping(value = "/OdAuthorisationSearch", method = RequestMethod.POST)
	public @ResponseBody String OdAuthorisationSearch(@RequestBody EmployeeLeavedetailsInfo empLeaveDet) {
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");
		return genfac.getGenFac().objectArrayToJsonConvertor(leavefac.getLeaveFac().getOdAuthSearchDetails(empLeaveDet))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/getAllEmployee/123
	 *               here "123" is the value provided by you this method will retun
	 *               the list of emplyee details as json string
	 */
	@RequestMapping(value = "/getEmployeeOdDetails", method = RequestMethod.GET)
	public @ResponseBody String getEmployeeOdDetailsList() {
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");
		return genfac.getGenFac().objectArrayToJsonConvertor(
				leavefac.getLeaveFac().getEmployeeOdDetails(genfac.getGenFac().getCurrentUserId())).toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/cancelOd/
	 * 
	 */
	@RequestMapping(value = "/cancelOd", method = RequestMethod.POST)
	public @ResponseBody String cancelOd(@RequestBody List<EmployeeLeavedetailsInfo> empLeaveDetList) {
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");
		return genfac.getGenFac().objectArrayToJsonConvertor(leavefac.getLeaveFac().cancelEmployeeOd(empLeaveDetList))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/leaveHistorySearch/
	 *               here "123" is the value provided by you is an employeeId
	 */
	@RequestMapping(value = "/OdCancelSearch", method = RequestMethod.POST)
	public @ResponseBody String OdCancelSearch(@RequestBody EmployeeLeavedetailsInfo empLeaveDet) {
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");
		empLeaveDet.setEmpIdIn(genfac.getGenFac().getCurrentUserId());
		return genfac.getGenFac().objectArrayToJsonConvertor(leavefac.getLeaveFac().OdCancelSearch(empLeaveDet))
				.toString();
	}

	/**
	 * Employee Time change authorization request
	 */
	@RequestMapping(value = "/empAttendaceAuthorizationRequest", method = RequestMethod.POST)
	public @ResponseBody String empAttendaceAuthorizationRequest(
			@RequestBody List<Attendancedetails> empAuthorizationEdit) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		empfac.getEmpFac().attendanceAuthorizationRequest(empAuthorizationEdit);
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(
						empfac.getEmpFac().getAttendanceDetailsByRoleForAuth(genfac.getGenFac().getCurrentUserId()))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say
	 *               http://localhost:8080/ESS/api/emplyee/attendanceDetailsByRoleForAuth/
	 *               here "123" is the value provided by you is an employeeId
	 */
	@RequestMapping(value = "/attendanceDetailsByRoleForAuth", method = RequestMethod.GET)
	public @ResponseBody String attendanceDetailsByRoleForAuth() {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(
						empfac.getEmpFac().getAttendanceDetailsByRoleForAuth(genfac.getGenFac().getCurrentUserId()))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say
	 *               http://localhost:8080/ESS/api/emplyee/empAttendanceDetailsApprovalSearch/
	 *               here "123" is the value provided by you is an employeeId
	 */
	@RequestMapping(value = "/empAttendanceDetailsApprovalSearch", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody String empAttendanceDetailsApprovalSearch(
			@RequestBody EmployeeAttendanceDetails empAttendanceDetails) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		if (genfac.getGenFac().getCurrentUserDetails().getRole().equals(EssConstants.ROLE_SUPERVISOR)
				|| genfac.getGenFac().getCurrentUserDetails().getRole().equals(EssConstants.USER_ROLE_TRAINEE)) {
			empAttendanceDetails.setEmpId(genfac.getGenFac().getCurrentUserId());
		}
		return genfac.getGenFac().objectArrayToJsonConvertor(
				empfac.getEmpFac().getAttendanceReportDetailsAuthSearch(empAttendanceDetails)).toString();
	}

	/**
	 * Employee Time Change approval
	 */
	@RequestMapping(value = "/empAttendaceAuthorizationRequestApproval", method = RequestMethod.POST)
	public @ResponseBody String empAttendaceAuthorizationRequestApproval(
			@RequestBody List<Attendancedetails> empAuthorizationEdit) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		empfac.getEmpFac().attendanceAuthorizationRequestApproval(empAuthorizationEdit);
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(
						empfac.getEmpFac().getAttendanceDetailsByRoleForAuth(genfac.getGenFac().getCurrentUserId()))
				.toString();
	}

	/**
	 * Employee Time Change Rejection
	 */
	@RequestMapping(value = "/empAttendaceAuthorizationRequestReject", method = RequestMethod.POST)
	public @ResponseBody String empAttendaceAuthorizationRequestReject(
			@RequestBody List<Attendancedetails> empAuthorizationEdit) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		empfac.getEmpFac().empAttendaceAuthorizationRequestReject(empAuthorizationEdit);
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(
						empfac.getEmpFac().getAttendanceDetailsByRoleForAuth(genfac.getGenFac().getCurrentUserId()))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say
	 *               http://localhost:8080/ESS/api/emplyee/empAttendanceDetailsSearch/
	 *               here "123" is the value provided by you is an employeeId
	 */
	@RequestMapping(value = "/empAttendanceRequestDetailsSearch", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody String empAttendanceRequestDetailsSearch(
			@RequestBody EmployeeAttendanceDetails empAttendanceDetails) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		if (genfac.getGenFac().getCurrentUserDetails().getRole().equals(EssConstants.USER_ROLE_SUPERVISOR)
				|| genfac.getGenFac().getCurrentUserDetails().getRole().equals(EssConstants.USER_ROLE_TRAINEE)
				|| genfac.getGenFac().getCurrentUserDetails().getRole().equals(EssConstants.USER_ROLE_EMPLOYEE)) {
			empAttendanceDetails.setEmpId(genfac.getGenFac().getCurrentUserId());
		}
		return genfac.getGenFac()
				.objectArrayToJsonConvertor(empfac.getEmpFac().empAttendanceRequestDetailsSearch(empAttendanceDetails))
				.toString();
	}

	/**
	 * @PathVariable Example suppose you want to call an api by providing a value
	 *               say http://localhost:8080/ESS/api/emplyee/isOdAlreadyApplied/
	 *               here "123" is the value provided by you is an employeeId
	 */
	@RequestMapping(value = "/isOdAlreadyApplied", method = RequestMethod.POST)
	public @ResponseBody List<String> isOdAlreadyApplied(@RequestBody EmployeeLeavedetailsInfo empAppliedOdDet) {
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");
		List<String> obj = new ArrayList<String>();
		empAppliedOdDet.setEmpIdIn(genfac.getGenFac().getCurrentUserId());
		boolean flag = leavefac.getLeaveFac().isOdAlreadyApplied(empAppliedOdDet);
		String response = null;
		if (flag == true) {
			response = "Success";
		} else {
			response = "Failure";
		}
		obj.add(response);
		return obj;
	}

	@GET
	// @Path("get")
	@RequestMapping(value = "/shortfallReportDays", method = RequestMethod.GET, params = { "fromDate", "toDate" })
	public @ResponseBody String getShortfallDaysReport(@RequestParam(value = "fromDate") String fromDate,
			@RequestParam(value = "toDate") String toDate) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		String empId = genfac.getGenFac().getCurrentUserId();
		String bySupervisor = "undefined";
		List<AttendanceDaysReportDetails> ad = empfac.getEmpFac().getAttendanceDaysReport(fromDate, toDate, empId,
				bySupervisor);
		return genfac.getGenFac().objectArrayToJsonConvertor(ad).toString();
	}

	@GET
	// @Path("get")
	@RequestMapping(value = "/shortfallReportHours", method = RequestMethod.GET, params = { "fromDate", "toDate" })
	public @ResponseBody String getShortfallHoursReport(@RequestParam(value = "fromDate") String fromDate,
			@RequestParam(value = "toDate") String toDate) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		String empId = genfac.getGenFac().getCurrentUserId();
		String bySupervisor = "undefined";
		List<AttendanceHoursReportDetails> adhrsReport = empfac.getEmpFac().getAttendanceHrsReport(fromDate, toDate,
				empId, bySupervisor);
		return genfac.getGenFac().objectArrayToJsonConvertor(adhrsReport).toString();
	}

	@RequestMapping(value = "/isLeaveOrOdAlreadyApplied", method = RequestMethod.POST)
	public @ResponseBody List<String> isLeaveOrOdAlreadyApplied(
			@RequestBody EmployeeLeavedetailsInfo empAppliedLeaveOrOdDet) {
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");
		List<String> obj = new ArrayList<String>();
		empAppliedLeaveOrOdDet.setEmpIdIn(genfac.getGenFac().getCurrentUserId());// leavealready applied
		boolean flag = leavefac.getLeaveFac().isLeaveAlreadyApplied(empAppliedLeaveOrOdDet);
		empAppliedLeaveOrOdDet.setEmpIdIn(genfac.getGenFac().getCurrentUserId());
		boolean flag1 = leavefac.getLeaveFac().isOdAlreadyApplied(empAppliedLeaveOrOdDet);// od already applied
		String response = null;
		if (flag == false && flag1 == true) {
			response = "Success";
		} else if (flag == true && flag1 == false) {
			response = "Success";
		} else {
			response = "Failure";
		}
		obj.add(response);
		return obj;

	}

	@RequestMapping(value = "/getTimedetails", method = RequestMethod.POST)
	public @ResponseBody List<String> getTimedetails(@RequestBody Attendancedetails attendance) {
		EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		List<String> obj = new ArrayList<String>();
		String response=null;
		try {
			obj.add(genfac.getGenFac().objectToJsonConvertor(empfac.getEmpFac().timedetails(attendance)).toString());
			return obj;
		} catch (Exception mx) {
			LOGGER.info("getTodayAttendanceDetailsByID" + mx);
		}
		obj.add(response);
		return obj;
	}
	
	@RequestMapping(value="/getLeaveDetailsInfoById",method = RequestMethod.POST)
	public @ResponseBody String getempLeaveDetailByDate(@RequestBody oddetails odempdetai) {
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");
		String response="false";
		try {
		List<Leavedetails> leavedetailbyemp=(leavefac.getLeaveFac().getEmployeeLeaveDetailsByDate(odempdetai));
		System.out.println("obj "+leavedetailbyemp.get(0).getNoofDays());
		if(leavedetailbyemp.get(0).getNoofDays()==0.5) {		
			List<oddetails> addodleave=leavefac.getLeaveFac().addOdAndLeave(odempdetai);
			response="true";	
		}
		return response;
		}catch(Exception e) {
			System.out.println(e);
			return response;
		}
	}

	@RequestMapping(value="/getODDetailsInfoById",method = RequestMethod.POST)
	public @ResponseBody String getempOdDetailsByDate(@RequestBody EmployeeLeavedetailsInfo leaveempdetai) {
		LeaveFactory leavefac = factory.getFactory("LeaveFactory", "LeaveDetailsImpl");
		String response="false";
		try {
		List<oddetails> oddetailbyemp=(leavefac.getLeaveFac().getemployeeOdDetailsByDate(leaveempdetai));
		System.out.println("empde "+oddetailbyemp.get(0).getNoofDays());
		if(oddetailbyemp.get(0).getNoofDays()==0.5) {
			List<Leavedetails> addleaveod=leavefac.getLeaveFac().addLeaveAndOd(leaveempdetai);
			response="true";	
		}
		
//		System.out.println("db "+oddetailbyemp.get(0));
		return response;	
		}catch(Exception e) {
			return response;
		}
	}
	
	
	
	@RequestMapping(value = "/getAllEngineeringdetails", method = RequestMethod.GET)
	public @ResponseBody String getAllEmployee1() {
		EmpFactory empfac = this.factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		GeneralFactory genfac = this.factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		return genfac.getGenFac().objectArrayToJsonConvertor(empfac.getEmpFac().getEmployeesReportingPerson1()).toString();

		}
	
	  @RequestMapping(value = "/updateAllEngineeringdetails", method = RequestMethod.POST)
		public @ResponseBody List<String> updateEngineeringdetails(@RequestBody EngineeringService eng) {
			EmpFactory empfac = this.factory.getFactory("EmpFactory", "EmployeeServiceImpl");
			GeneralFactory genfac = this.factory.getFactory("GeneralFactory", "GeneralServiceImpl");
			List<String> obj = new ArrayList<String>();
			String respone;
			if (empfac.getEmpFac().saveEngineering(eng) != null) {
				respone = "Success";
				obj.add(respone);
			} else {
				respone = "Failure";
				obj.add(respone);
			}
			return obj;
		}
	  
	  
	  @RequestMapping(value = "/getAllEngineeringEmployeeDetails", method = RequestMethod.POST)
		public @ResponseBody List<EngineeringService> getAllEngineeringEmployeeDetails(@RequestBody EngineeringService eng) {
			EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
	       return	empfac.getEmpFac().getEngineeringEmployeeDetails(eng);
					
		}
	  @RequestMapping(value="/EngineerEmployeeNames", method = RequestMethod.GET)
	  public @ResponseBody String getAllEngineeringEmployeeDetailsDropDown()
	  {
		  EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
		  GeneralFactory genfac = this.factory.getFactory("GeneralFactory", "GeneralServiceImpl");
		  System.out.println(empfac.getEmpFac().getEngineeringEmployees());
		  return genfac.getGenFac().objectArrayToJsonConvertor(empfac.getEmpFac().getEngineeringEmployees()).toString();
		  
	  }
	  @RequestMapping(value="/isEngineeringEmployee", method = RequestMethod.GET)
		public List<String> isEngineeringEmployee()
		{
		  List<String> obj = new ArrayList<String>();
			EmpFactory empfac = factory.getFactory("EmpFactory", "EmployeeServiceImpl");
			String response;
		if(empfac.getEmpFac().isEngineeringEmployee())
			{response="success";
			obj.add(response); 
			}else{response="failure";
			obj.add(response);
			}
			return obj;
		
		}
	  
	
	
	

}