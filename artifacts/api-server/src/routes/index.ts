import { Router, type IRouter } from "express";
import healthRouter from "./health";
import authRouter from "./auth";
import ridesRouter from "./rides";
import tripsRouter from "./trips";
import bookingsRouter from "./bookings";
import ticketsRouter from "./tickets";
import paymentsRouter from "./payments";
import usersRouter from "./users";
import adminRouter from "./admin";
import parcelsRouter from "./parcels";
import matchesRouter from "./matches";

const router: IRouter = Router();

router.use(healthRouter);
router.use(authRouter);
router.use(ridesRouter);
router.use(tripsRouter);
router.use(bookingsRouter);
router.use(ticketsRouter);
router.use(paymentsRouter);
router.use(usersRouter);
router.use(adminRouter);
router.use(parcelsRouter);
router.use(matchesRouter);

export default router;
