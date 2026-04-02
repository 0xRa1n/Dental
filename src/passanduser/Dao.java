package passanduser;

import java.sql.*;
import model.User;

import java.time.*;
import java.util.*;

import application.Main.App.DoctorAvailability;

public class Dao {
    
    public static List<String> getDoctorServices(String doctorUsername) {
        List<String> services = new ArrayList<>();
        // Join users, doctor_services, and services tables to resolve the authorized list
        String sql = "SELECT s.service_name FROM services s " +
                     "INNER JOIN doctor_services ds ON s.id = ds.service_id " +
                     "INNER JOIN users u ON ds.doctor_id = u.id " +
                     "WHERE u.username = ?";

        try (Connection con = Dbconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, doctorUsername);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    services.add(rs.getString("service_name"));
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to load doctor services: " + e.getMessage());
        }
        return services;
    }

	public static void saveDoctorServices(String doctorUsername, List<String> selectedServices) {
	    // Step 1: Retrieve the doctor's ID from the users table
	    String getDoctorIdSql = "SELECT id FROM users WHERE username = ?";

	    // Step 2: Delete existing services to cleanly apply the updated list
	    String deleteOldServicesSql = "DELETE FROM doctor_services WHERE doctor_id = ?";

	    // Step 3: Insert the new services by mapping the string to the service ID
	    String insertServiceSql = "INSERT INTO doctor_services (doctor_id, service_id) " +
	                              "VALUES (?, (SELECT id FROM services WHERE service_name = ?))";

	    try (Connection con = Dbconnection.getConnection()) {
	        con.setAutoCommit(false); // Begin transaction for atomicity

	        int doctorId = -1;

	        // Execute Step 1
	        try (PreparedStatement psGetId = con.prepareStatement(getDoctorIdSql)) {
	            psGetId.setString(1, doctorUsername);
	            try (ResultSet rs = psGetId.executeQuery()) {
	                if (rs.next()) {
	                    doctorId = rs.getInt("id");
	                }
	            }
	        }

	        if (doctorId == -1) {
	            System.out.println("Error: Doctor username not found in database.");
	            return;
	        }

	        // Execute Step 2
	        try (PreparedStatement psDelete = con.prepareStatement(deleteOldServicesSql)) {
	            psDelete.setInt(1, doctorId);
	            psDelete.executeUpdate();
	        }

	        // Execute Step 3
	        try (PreparedStatement psInsert = con.prepareStatement(insertServiceSql)) {
	            for (String serviceName : selectedServices) {
	                psInsert.setInt(1, doctorId);
	                psInsert.setString(2, serviceName);
	                psInsert.addBatch(); // Use batching for efficiency
	            }
	            psInsert.executeBatch();
	        }

	        con.commit(); // Commit the transaction
	        System.out.println("Services successfully updated for doctor: " + doctorUsername);

	    } catch (Exception e) {
	        System.out.println("Database operation failed: " + e.getMessage());
	    }
	}
	public static DoctorAvailability getDoctorAvailability(String doctorName) {
	    DoctorAvailability avail = new DoctorAvailability();
	    
	    // Fetch Time Window
	    String sqlSchedule = "SELECT start_time, end_time FROM doctor_schedule WHERE dentist = ?";
	    // Fetch Recurring Days
	    String sqlDays = "SELECT day_of_week FROM doctor_recurring_days WHERE schedule_id IN (SELECT id FROM doctor_schedule WHERE dentist = ?)";
	    // Fetch Blocked Dates
	    String sqlBlocked = "SELECT blocked_date FROM doctor_blocked_dates WHERE dentist = ?";

	    try (Connection con = Dbconnection.getConnection()) {
	        
	        try (PreparedStatement ps = con.prepareStatement(sqlSchedule)) {
	            ps.setString(1, doctorName);
	            ResultSet rs = ps.executeQuery();
	            if (rs.next()) {
	                avail.startTime = rs.getString("start_time");
	                avail.endTime = rs.getString("end_time");
	            }
	        }

	        try (PreparedStatement ps = con.prepareStatement(sqlDays)) {
	            ps.setString(1, doctorName);
	            ResultSet rs = ps.executeQuery();
	            while (rs.next()) {
	                avail.recurringDays.add(rs.getString("day_of_week"));
	            }
	        }

	        try (PreparedStatement ps = con.prepareStatement(sqlBlocked)) {
	            ps.setString(1, doctorName);
	            ResultSet rs = ps.executeQuery();
	            while (rs.next()) {
	                avail.blockedDates.add(LocalDate.parse(rs.getString("blocked_date")));
	            }
	        }
	    } catch (Exception e) {
	        System.out.println("Error retrieving schedule: " + e.getMessage());
	    }
	    return avail;
	}
	public static void saveDoctorAvailability(String doctorName, Set<DayOfWeek> days, String start, String end, Set<LocalDate> blocked) {
	    String deleteSchedule = "DELETE FROM doctor_schedule WHERE dentist = ?";
	    String deleteDays = "DELETE FROM doctor_recurring_days WHERE schedule_id IN (SELECT id FROM doctor_schedule WHERE dentist = ?)";
	    String deleteBlocked = "DELETE FROM doctor_blocked_dates WHERE dentist = ?";

	    String insertSchedule = "INSERT INTO doctor_schedule (dentist, start_time, end_time) VALUES (?, ?, ?)";
	    String insertDay = "INSERT INTO doctor_recurring_days (schedule_id, day_of_week) VALUES (?, ?)";
	    String insertBlocked = "INSERT INTO doctor_blocked_dates (dentist, blocked_date) VALUES (?, ?)";

	    try (Connection con = Dbconnection.getConnection()) {
	        con.setAutoCommit(false); // Begin transaction

	        // 1. Clear existing data to prevent duplicates
	        try (PreparedStatement psDays = con.prepareStatement(deleteDays);
	             PreparedStatement psSched = con.prepareStatement(deleteSchedule);
	             PreparedStatement psBlock = con.prepareStatement(deleteBlocked)) {

	            psDays.setString(1, doctorName);
	            psDays.executeUpdate();

	            psSched.setString(1, doctorName);
	            psSched.executeUpdate();

	            psBlock.setString(1, doctorName);
	            psBlock.executeUpdate();
	        }

	        // 2. Insert new time window
	        int scheduleId = -1;
	        try (PreparedStatement psInSched = con.prepareStatement(insertSchedule, PreparedStatement.RETURN_GENERATED_KEYS)) {
	            psInSched.setString(1, doctorName);
	            psInSched.setString(2, start);
	            psInSched.setString(3, end);
	            psInSched.executeUpdate();

	            try (ResultSet rs = psInSched.getGeneratedKeys()) {
	                if (rs.next()) {
	                    scheduleId = rs.getInt(1);
	                }
	            }
	        }

	        // 3. Insert recurring days
	        if (scheduleId != -1) {
	            try (PreparedStatement psInDay = con.prepareStatement(insertDay)) {
	                for (DayOfWeek day : days) {
	                    psInDay.setInt(1, scheduleId);
	                    psInDay.setString(2, day.name());
	                    psInDay.addBatch();
	                }
	                psInDay.executeBatch();
	            }
	        }

	        // 4. Insert blocked dates
	        try (PreparedStatement psInBlock = con.prepareStatement(insertBlocked)) {
	            for (LocalDate date : blocked) {
	                psInBlock.setString(1, doctorName);
	                psInBlock.setString(2, date.toString());
	                psInBlock.addBatch();
	            }
	            psInBlock.executeBatch();
	        }

	        con.commit(); // Commit transaction
	    } catch (Exception e) {
	        System.out.println("Error saving schedule: " + e.getMessage());
	    }
	}
	public static String getAppointmentNotes(int id) {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			con = Dbconnection.getConnection();
			if (con == null) {
				System.out.println("❌ Database connection failed!");
				return null;
			}

			String sql = "SELECT notes FROM appointments WHERE id=?";
			ps = con.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();	

			if (rs.next()) {
				String notes = rs.getString("notes");
				return notes;
			} else {
				System.out.println("❌ No appointment found with ID " + id);
			}

		} catch (Exception e) {
			System.out.println("❌ Get Notes Error: " + e.getMessage());
		} finally {
			closeSafely(rs, ps, con);
		}
		return null;
	}
	
	public static void addAppointmentNotes(int id, String notes) {
		Connection con = null;
		PreparedStatement ps = null;
		
		try {
			con = Dbconnection.getConnection();
			if (con == null) {
				System.out.println("❌ Database connection failed!");
				return;
			}

			String sql = "UPDATE appointments SET notes=? WHERE id=?";
			ps = con.prepareStatement(sql);
			ps.setString(1, notes);
			ps.setInt(2, id);

			boolean success = ps.executeUpdate() > 0;
			if(success) System.out.println("✅ Notes added for appointment ID " + id);

		} catch (Exception e) {
			System.out.println("❌ Add Notes Error: " + e.getMessage());
		} finally {
			closeSafely(null, ps, con);
		}
		
	}
	
	public static boolean updateUser(int id, String name, String username, String password, String email, String role) {
		Connection con = null;
		PreparedStatement ps = null;
		
		try {
			con = Dbconnection.getConnection();
			if (con == null) {
				System.out.println("❌ Database connection failed!");
				return false;
			}

			String sql = "UPDATE users SET full_name=?, username=?, password=?, email=?, role=? WHERE id=?";
			ps = con.prepareStatement(sql);
			ps.setString(1, name);
			ps.setString(2, username);
			ps.setString(3, password);
			ps.setString(4, email);
			ps.setString(5, role);
			ps.setInt(6, id);

			boolean success = ps.executeUpdate() > 0;
			if(!success) {
				System.out.println("❌ No user found with ID " + id);
				return false;
			}
			return true;
		} catch (Exception e) {
			System.out.println("❌ Update Error: " + e.getMessage());
		} finally {
			closeSafely(null, ps, con);
		}
		return false;
	}
	
	public static void updateAppointmentStatus(int id, String newStatus) {
		Connection con = null;
		PreparedStatement ps = null;
		
		try {
			con = Dbconnection.getConnection();
			if (con == null) {
				System.out.println("❌ Database connection failed!");
				return;
			}

			String sql = "UPDATE appointments SET status=? WHERE id=?";
			ps = con.prepareStatement(sql);
			ps.setString(1, newStatus);
			ps.setInt(2, id);

			boolean success = ps.executeUpdate() > 0;
			if(success) System.out.println("✅ Appointment status updated to '" + newStatus + "' for ID " + id);

		} catch (Exception e) {
			System.out.println("❌ Status Update Error: " + e.getMessage());
		} finally {
			closeSafely(null, ps, con);
		}
		
	}
	public static void deleteBooking(int id) {
		Connection con = null;
		PreparedStatement ps = null;
		
		try {
			con = Dbconnection.getConnection();
			if (con == null) {
				System.out.println("❌ Database connection failed!");
				return;
			}

			String sql = "DELETE FROM appointments WHERE id=?";
			ps = con.prepareStatement(sql);
			ps.setInt(1, id);

			boolean success = ps.executeUpdate() > 0;
			if(success) System.out.println("✅ Appointment deleted for " + id);

		} catch (Exception e) {
			System.out.println("❌ Deletion Error: " + e.getMessage());
		} finally {
			closeSafely(null, ps, con);
		}
		
	}
	public static void deleteUser(int id) {
		Connection con = null;
		PreparedStatement ps = null;
		
		try {
			con = Dbconnection.getConnection();
			if (con == null) {
				System.out.println("❌ Database connection failed!");
				return;
			}

			String sql = "DELETE FROM users WHERE id=?";
			ps = con.prepareStatement(sql);
			ps.setInt(1, id);

			boolean success = ps.executeUpdate() > 0;
			if(success) System.out.println("✅ User deleted");

		} catch (Exception e) {
			System.out.println("❌ Deletion Error: " + e.getMessage());
		} finally {
			closeSafely(null, ps, con);
		}
		
	}
	public static void updateBooking(String username, String date, String serviceTime, String dentist, String dentalService, String status) {
		Connection con = null;
		PreparedStatement ps = null;
		
		try {
			
			
			con = Dbconnection.getConnection();
			if (con == null) {
				System.out.println("❌ Database connection failed!");
				return;
			}

			String sql = "UPDATE appointments SET date=?, serviceTime=?, dentist=?, dentalService=? WHERE username=?";
			ps = con.prepareStatement(sql);
			ps.setString(1, date);
			ps.setString(2, serviceTime);
			ps.setString(3, dentist);
			ps.setString(4, dentalService);
			ps.setString(5, username);

			boolean success = ps.executeUpdate() > 0;
			if(success) System.out.println("✅ Appointment updated for " + username);

		} catch (Exception e) {
			System.out.println("❌ Update Error: " + e.getMessage());
		} finally {
			closeSafely(null, ps, con);
		}
		
	}
	public static void bookAppointment(String username, String date, String serviceTime, String dentist, String dentalService) {
		Connection con = null;
		PreparedStatement ps = null;
		
		try {
			con = Dbconnection.getConnection();
			if (con == null) {
				System.out.println("❌ Database connection failed!");
				return;
			}

			String sql = "INSERT INTO appointments (username, date, serviceTime, dentist, dentalService, status, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
			ps = con.prepareStatement(sql);
			ps.setString(1, username);
			ps.setString(2, date);
			ps.setString(3, serviceTime);
			ps.setString(4, dentist);
			ps.setString(5, dentalService);
			ps.setString(6, "Pending");
			ps.setString(7, ""); // Default empty notes

			boolean success = ps.executeUpdate() > 0;
			if(success) System.out.println("✅ Appointment booked for " + username + " with Dr. " + dentist + " on " + date);

		} catch (Exception e) {
			System.out.println("❌ Booking Error: " + e.getMessage());
		} finally {
			closeSafely(null, ps, con);
		}
		
	}
	
	public static void readAppointments(String username) {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			con = Dbconnection.getConnection();
			if (con == null) {
				System.out.println("❌ Database connection failed!");
				return;
			}

			String sql = "SELECT * FROM appointments WHERE username=?";
			ps = con.prepareStatement(sql);
			ps.setString(1, username);
			rs = ps.executeQuery();	

			System.out.println("📅 Appointments for " + username + ":");
			while (rs.next()) {
				System.out.println("- " + rs.getString("date") + " at " + rs.getString("serviceTime") + " with Dr. " + rs.getString("dentist") + " (" + rs.getString("dentalService") + ")");
			}

		} catch (Exception e) {
			System.out.println("❌ Read Error: " + e.getMessage());
		} finally {
			closeSafely(rs, ps, con);
		}
	}
    // 🔐 LOGIN WITH ACTIVITY TRACKING
    public static User login(String username, String password) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            con = Dbconnection.getConnection();
            if (con == null) {
                System.out.println("❌ Database connection failed!");
                return null;
            }

            // ✅ Ensure tables exist
            createUsersTable(con);
            createLoginActivityTable(con);

            String sql = "SELECT * FROM users WHERE (username=? OR email=?) AND password=?";
            ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, username);
            ps.setString(3, password);

            rs = ps.executeQuery();	

            if (rs.next()) {
                User user = new User(
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("full_name"),
                    rs.getString("role")
                );

                // ✅ SUCCESS LOGIN LOG
                logLoginActivity(con, user, true);

                System.out.println("✅ Login successful: " + user.getUsername());
                return user;
            } else {
                // ❌ FAILED LOGIN LOG
                logLoginActivity(con, new User(username, "", "", "", "UNKNOWN"), false);
            }

        } catch (Exception e) {
            System.out.println("❌ Login Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeSafely(rs, ps, con);
        }
        return null;
    }

    // ✅ LOGIN ACTIVITY LOGGER
    private static void logLoginActivity(Connection con, User user, boolean success) {
        PreparedStatement ps = null;
        try {
            String sql = """
                INSERT INTO login_activity (
                    username, role, full_name, login_time, session_id, login_status
                ) VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, ?)
                """;

            ps = con.prepareStatement(sql);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getRole());
            ps.setString(3, user.getFull_name());
            ps.setString(4, java.util.UUID.randomUUID().toString().substring(0, 8));
            ps.setString(5, success ? "SUCCESS" : "FAILED");

            ps.executeUpdate();

            System.out.println("📊 Logged " + (success ? "SUCCESS" : "FAILED") + ": " + user.getUsername());

        } catch (SQLException e) {
            System.out.println("❌ Logging failed: " + e.getMessage());
        } finally {
            closeSafely(null, ps, null);
        }
    }

    // ✅ AUTO CREATE USERS TABLE
    private static void createUsersTable(Connection con) {
        try {
            String sql = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    full_name TEXT NOT NULL,
                    role TEXT DEFAULT 'patient',
                    login_time DATETIME DEFAULT CURRENT_TIMESTAMP
                )
                """;
            con.createStatement().execute(sql);
        } catch (SQLException e) {
            System.out.println("❌ Users table creation failed: " + e.getMessage());
        }
    }

    // ✅ AUTO CREATE LOGIN ACTIVITY TABLE
    private static void createLoginActivityTable(Connection con) {
        try {
            String sql = """
                CREATE TABLE IF NOT EXISTS login_activity (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL,
                    role TEXT,
                    full_name TEXT,
                    login_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                    session_id TEXT,
                    login_status TEXT
                )
                """;
            con.createStatement().execute(sql);
        } catch (SQLException e) {
            System.out.println("❌ login_activity table creation failed: " + e.getMessage());
        }
    }

    // 📝 REGISTER USER
    public static boolean registerUser(User u) {
        Connection con = null;
        PreparedStatement checkPs = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        
        try {
            con = Dbconnection.getConnection();
            if (con == null) return false;

            // ✅ Ensure tables exist
            createUsersTable(con);
            createLoginActivityTable(con);

            String checkSql = "SELECT COUNT(*) FROM users WHERE username=? OR email=?";
            checkPs = con.prepareStatement(checkSql);
            checkPs.setString(1, u.getUsername());
            checkPs.setString(2, u.getEmail());
            rs = checkPs.executeQuery();
            rs.next();
            
            if (rs.getInt(1) > 0) {
                System.out.println("❌ User exists: " + u.getUsername());
                return false;
            }

            String sql = "INSERT INTO users(username,email,password,full_name,role) VALUES(?,?,?,?,?)";
            ps = con.prepareStatement(sql);
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPassword());
            ps.setString(4, u.getFull_name());
            ps.setString(5, u.getRole());

            boolean success = ps.executeUpdate() > 0;
            if(success) System.out.println("✅ Registered: " + u.getUsername());
            return success;

        } catch (Exception e) {
            System.out.println("❌ Register Error: " + e.getMessage());
            return false;
        } finally {
            closeSafely(rs, checkPs, con);
            closeSafely(null, ps, null);
        }
    }

    // 🧹 Close safely
    private static void closeSafely(ResultSet rs, PreparedStatement ps, Connection con) {
        try {
            if(rs != null) rs.close();
            if(ps != null) ps.close();
            if(con != null) con.close();
        } catch (SQLException e) {}
    }

    // 📊 GET RECENT LOGINS
    public static ResultSet getRecentLogins(int limit) {
        try {
            Connection con = Dbconnection.getConnection();
            String sql = "SELECT * FROM login_activity ORDER BY login_time DESC LIMIT ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, limit);
            return ps.executeQuery();
        } catch (Exception e) {
            return null;
        }
    }
}