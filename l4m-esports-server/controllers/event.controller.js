import * as eventService from '../services/event.service.js';
import { emitToAll } from '../services/socket.service.js';

export const getAllEvents = async (req, res, next) => {
  try {
    const events = await eventService.getAllEvents(req.query);
    res.status(200).json({ success: true, data: { events } });
  } catch (error) { next(error); }
};

export const getEventById = async (req, res, next) => {
  try {
    const event = await eventService.getEventById(req.params.id);
    res.status(200).json({ success: true, data: { event } });
  } catch (error) { next(error); }
};

export const getEventsNearby = async (req, res, next) => {
  try {
    // On s'attend à ?long=-71.xxx&lat=45.xxx
    const { long, lat, distance } = req.query;
    const events = await eventService.getEventsNearby(long, lat, distance);
    res.status(200).json({ success: true, data: { events } });
  } catch (error) { next(error); }
};

export const createEvent = async (req, res, next) => {
  try {
    const event = await eventService.createEvent(req.body, req.user._id);
    // Emit socket event for real-time sync
    emitToAll('event:created', { event });
    res.status(201).json({
      success: true,
      message: 'Event created successfully',
      data: { event }
    });
  } catch (error) { next(error); }
};

export const updateEvent = async (req, res, next) => {
  try {
    const event = await eventService.updateEvent(req.params.id, req.body);
    // Emit socket event for real-time sync
    emitToAll('event:updated', { eventId: req.params.id, event });
    res.status(200).json({
      success: true,
      message: 'Event updated successfully',
      data: { event }
    });
  } catch (error) { next(error); }
};

export const deleteEvent = async (req, res, next) => {
  try {
    await eventService.deleteEvent(req.params.id);
    // Emit socket event for real-time sync
    emitToAll('event:deleted', { eventId: req.params.id });
    res.status(200).json({ success: true, message: 'Event deleted successfully' });
  } catch (error) { next(error); }
};

export const generateBracket = async (req, res, next) => {
  try {
    const bracket = await eventService.generateBracket(req.params.id);
    res.status(200).json({
      success: true,
      message: 'Bracket generated successfully',
      data: { bracket }
    });
  } catch (error) { next(error); }
};

export const getEventBracket = async (req, res, next) => {
  try {
    const bracket = await eventService.getEventBracket(req.params.id);
    res.status(200).json({ success: true, data: { bracket } });
  } catch (error) { next(error); }
};