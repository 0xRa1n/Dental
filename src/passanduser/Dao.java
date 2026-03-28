package passanduser;

import java.sql.*;
import model.User;
import java.util.Optional;
public class Dao {
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

			String sql = "INSERT INTO appointments (username, date, serviceTime, dentist, dentalService, status) VALUES (?, ?, ?, ?, ?, ?)";
			ps = con.prepareStatement(sql);
			ps.setString(1, username);
			ps.setString(2, date);
			ps.setString(3, serviceTime);
			ps.setString(4, dentist);
			ps.setString(5, dentalService);
			ps.setString(6, "Pending");
			

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