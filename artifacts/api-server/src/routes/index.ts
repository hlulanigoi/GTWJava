import { Router, type IRouter } from "express";
import healthRouter from "./health";
import authRouter from "./auth";
import parcelsRouter from "./parcels";
import tripsRouter from "./trips";
import matchesRouter from "./matches";
import ticketsRouter from "./tickets";
import paymentsRouter from "./payments";
import usersRouter from "./users";
import adminRouter from "./admin";

const router: IRouter = Router();

router.use(healthRouter);
router.use(authRouter);
router.use(parcelsRouter);
router.use(tripsRouter);
router.use(matchesRouter);
router.use(ticketsRouter);
router.use(paymentsRouter);
router.use(usersRouter);
router.use(adminRouter);

export default router;
