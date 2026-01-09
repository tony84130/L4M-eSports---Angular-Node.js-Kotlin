import * as registrationService from '../services/eventRegistration.service.js';

export const createRegistration = async (req, res, next) => {
  try {
    // On attend { teamId: "...", eventId: "...", participatingMemberIds: ["...", "..."] } dans le body
    const { teamId, eventId, participatingMemberIds } = req.body;
    const registration = await registrationService.createRegistration(
      req.user._id, 
      teamId, 
      eventId, 
      participatingMemberIds || []
    );
    res.status(201).json({ success: true, data: { registration } });
  } catch (error) { next(error); }
};

export const getRegistrations = async (req, res, next) => {
  try {
    const registrations = await registrationService.getRegistrations(req.query);
    res.status(200).json({ success: true, data: { registrations } });
  } catch (error) { next(error); }
};

export const getRegistrationsByEvent = async (req, res, next) => {
  try {
    const registrations = await registrationService.getRegistrations({ event: req.params.eventId });
    res.status(200).json({ success: true, data: { registrations } });
  } catch (error) { next(error); }
};

export const getRegistrationsByTeam = async (req, res, next) => {
  try {
    const registrations = await registrationService.getRegistrations({ team: req.params.teamId });
    res.status(200).json({ success: true, data: { registrations } });
  } catch (error) { next(error); }
};

export const getRegistrationById = async (req, res, next) => {
  try {
    const registration = await registrationService.getRegistrationById(req.params.id);
    res.status(200).json({ success: true, data: { registration } });
  } catch (error) { next(error); }
};

export const updateRegistration = async (req, res, next) => {
  try {
    const isAdmin = req.user.role === 'admin';
    const registration = await registrationService.updateRegistration(
      req.params.id,
      req.body,
      req.user._id,
      isAdmin
    );
    res.status(200).json({
      success: true,
      message: 'Registration updated successfully',
      data: { registration }
    });
  } catch (error) { next(error); }
};

export const deleteRegistration = async (req, res, next) => {
  try {
    // Vérification simple du rôle admin (à adapter selon ta gestion des rôles user)
    const isAdmin = req.user.role === 'admin';
    await registrationService.deleteRegistration(req.params.id, req.user._id, isAdmin);
    res.status(200).json({ success: true, message: 'Registration cancelled' });
  } catch (error) { next(error); }
};