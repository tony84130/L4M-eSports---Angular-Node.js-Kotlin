import User from '../models/user.model.js';
import env from '../config/env.js';

/**
 * Initialize default admin user on server startup
 * Uses environment variables if provided, otherwise creates a system admin
 */
export const initDefaultAdmin = async () => {
  try {
    // Check if admin already exists
    const existingAdmin = await User.findOne({ role: 'admin' });
    
    if (existingAdmin) {
      console.log('👤 Admin: already exists');
      return existingAdmin;
    }

    // Get admin credentials from environment variables
    const adminEmail = env.ADMIN_EMAIL || `admin-${Date.now()}@l4m-esports.local`;
    const adminPassword = env.ADMIN_PASSWORD || `admin-${Date.now()}-temp-change-me`;
    const adminFirstName = env.ADMIN_FIRST_NAME || 'Admin';
    const adminLastName = env.ADMIN_LAST_NAME || 'System';
    const adminGamertag = env.ADMIN_GAMERTAG || `Admin_${Date.now()}`;

    // Check if admin email already exists
    const existingUser = await User.findOne({ email: adminEmail.toLowerCase() });
    if (existingUser) {
      existingUser.role = 'admin';
      await existingUser.save();
      return existingUser;
    }

    // Create admin user
    const admin = await User.create({
      email: adminEmail.toLowerCase(),
      password: adminPassword,
      firstName: adminFirstName,
      lastName: adminLastName,
      gamertag: adminGamertag,
      role: 'admin'
    });
    
    if (!env.ADMIN_EMAIL || !env.ADMIN_PASSWORD) {
      console.log(`👤 Admin created: ${adminGamertag} (${adminEmail})`);
    }

    return admin;
  } catch (error) {
    console.error('❌ Error initializing default admin:', error);
    // Don't throw - allow server to start even if admin initialization fails
    // Return null so calling code can handle it
    return null;
  }
};

