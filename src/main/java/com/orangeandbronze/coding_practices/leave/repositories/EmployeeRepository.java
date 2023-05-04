package com.orangeandbronze.coding_practices.leave.repositories;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.orangeandbronze.coding_practices.leave.domain.Employee;

public class EmployeeRepository extends AbstractRepository {

	public Employee findById(int filerEmployeeId) {
		String sql = "select * from employees where employee_id = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, filerEmployeeId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				int vlCredits = rs.getInt("vl_credits");
				int slCredits = rs.getInt("sl_credits");
				int approverEmployeeId = rs.getInt("approver_employee_id");
				return new Employee(filerEmployeeId, vlCredits, slCredits, new Employee(approverEmployeeId, 0, 0, null));
			} else {
				throw new DataAccessException("Employee not found");
			}
		} catch (SQLException e) {
			throw new DataAccessException("Failed to query database", e);
		}
	}

}
