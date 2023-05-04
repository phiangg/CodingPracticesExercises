package com.orangeandbronze.coding_practices.leave.service;

import static java.time.DayOfWeek.*;

import java.sql.*;
import java.time.*;
import java.util.*;

import com.orangeandbronze.coding_practices.leave.domain.*;
import com.orangeandbronze.coding_practices.leave.domain.LeaveApplication.Status;
import com.orangeandbronze.coding_practices.leave.repositories.*;

/* TODO Notice that all the methods have to declare 'throws SQLException' since that's what the repository code throws. 
 * This breaks Single Responsibility Principle since a service should not be aware of any persistence details. The service
 * is now tightly coupled to the implementation of the Repositories. If the Repositories are changed to use something other
 * than JDBC, then all the 'throws SQLException' code needs to be changed as well.
 * Implement exception translation on the Repositories so that we no longer need to declare 'throws SQLException' in
 * the Service classes. Remove the 'throws SQLException' declarations after you have implemented exception translation. */
public class LeaveFilingService {

	private EmployeeRepository employeeRepo = new EmployeeRepository();
	private LeaveApplicationRepository leaveApplicationRepo = new LeaveApplicationRepository();

	/** For simplicity, only whole-day leaves are supported. No half-day leaves. **/
	public void fileLeave(int filerEmployeeId, LeaveType leaveType,
			LocalDate start, LocalDate end) throws SQLException {
		if (end.isBefore(start)) {
			throw new IllegalArgumentException(
					"End date before start date: Start: " + start + " End: "
							+ end);
		}
		Employee filer = employeeRepo.findById(filerEmployeeId);

		/*
		 * TODO Lots of business code down here. Can we push this into one of
		 * the domain classes? Which one would be appropriate?
		 */
		int leaveDays = 0;
		for (LocalDate currentDate = start; currentDate.isBefore(end)
				|| currentDate.isEqual(end); currentDate = currentDate
				.plusDays(1)) {

			// TODO Is there a way to make this code easier to read?
			// skip weekends
			if (currentDate.getDayOfWeek() == SATURDAY
					|| currentDate.getDayOfWeek() == SUNDAY) {
				continue;
			}
			
			/*
			 * TODO We're pulling out values from the Holiday enum in order to
			 * check if the currentDate is a holiday. Can we push this operation
			 * into the object that has the information?
			 */
			// skip holidays
			for (Holiday holiday : Holiday.values()) {
				if (MonthDay.from(currentDate).equals(holiday.getMonthDay())) {
					continue;
				}
			}
			// increase count of leave days
			leaveDays++;
		}
		// check if filer has enough credits
		int availableLeaveCredits = filer.getLeaveCreditsOfType(leaveType);
		if (availableLeaveCredits < leaveDays) {
			throw new InsufficientLeaveCreditsException("Employee " + filer
					+ " tried to file " + leaveDays + " days leave of type "
					+ leaveType + " but the employee only has "
					+ availableLeaveCredits + ' ' + leaveType
					+ " leave credits.");
		}
		LeaveApplication newLeaveApplication = new LeaveApplication(
				UUID.randomUUID(), filer, filer.getLeaveApprover(), leaveType,
				start, end, Status.PENDING);
		leaveApplicationRepo.create(newLeaveApplication);
	}

	public Collection<LeaveApplication> getPendingLeaveApplications(
			int filerEmployeeId) throws SQLException {
		return leaveApplicationRepo
				.findPendingByFilerEmployeeId(filerEmployeeId);
	}

	public void cancelLeaveApplication(int filerEmployeeId,
			UUID leaveApplicationId) throws SQLException {
		Employee filer = employeeRepo.findById(filerEmployeeId);
		LeaveApplication application = leaveApplicationRepo
				.findById(leaveApplicationId);
		filer.cancel(application);
		leaveApplicationRepo.updateStatus(application);
	}

	public void setEmployeeRepository(EmployeeRepository employeeRepo) {
		this.employeeRepo = employeeRepo;
	}

	public void setLeaveApplicationRepository(
			LeaveApplicationRepository leaveApplicationRepo) {
		this.leaveApplicationRepo = leaveApplicationRepo;
	}

}
