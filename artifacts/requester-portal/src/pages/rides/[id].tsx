import * as React from "react";
import { Link, useLocation } from "wouter";
import { useGetRide, useDeleteRide } from "@workspace/api-client-react";
import { Badge } from "@/components/ui/badge";
import { MapPin, Loader2, ArrowLeft, Trash2, Clock, CheckCircle2, User, Users, AlertCircle, Navigation } from "lucide-react";
import { format } from "date-fns";

export default function RideDetail({ params }: { params: { id: string } }) {
  const [, setLocation] = useLocation();
  const { data: ride, isLoading } = useGetRide(params.id);
  const deleteMut = useDeleteRide();

  if (isLoading) {
    return (
      <div className="flex-1 flex items-center justify-center min-h-[50vh]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  if (!ride) {
    return (
      <div className="p-8 text-center">
        <h2 className="text-2xl font-bold">Ride not found</h2>
        <Link href="/rides" className="text-primary mt-4 inline-block hover:underline">Back to rides</Link>
      </div>
    );
  }

  const handleDelete = () => {
    if (window.confirm("Are you sure you want to cancel this ride? This action cannot be undone.")) {
      deleteMut.mutate({ id: ride.id }, {
        onSuccess: () => {
          setLocation("/rides");
        }
      });
    }
  };

  const steps = [
    { status: 'PENDING', label: 'Requested', icon: Clock },
    { status: 'MATCHED', label: 'Driver Assigned', icon: User },
    { status: 'EN_ROUTE', label: 'On The Way', icon: Navigation },
    { status: 'COMPLETED', label: 'Completed', icon: CheckCircle2 },
  ];

  const currentStatusIndex = steps.findIndex(s => s.status === ride.status);
  const isCancelled = ride.status === 'CANCELLED';

  return (
    <div className="p-6 md:p-8 max-w-4xl mx-auto space-y-6">
      <Link href="/rides" className="inline-flex items-center text-sm font-medium text-muted-foreground hover:text-foreground transition-colors">
        <ArrowLeft className="h-4 w-4 mr-1.5" /> Back to rides
      </Link>

      <div className="flex flex-col md:flex-row md:items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl md:text-3xl font-bold text-foreground">Ride to {ride.destinationAddress?.split(',')[0]}</h1>
          <p className="text-muted-foreground mt-1">ID: {ride.id}</p>
        </div>
        <div className="flex items-center gap-3">
          <Badge status={ride.status} />
          {(ride.status === 'PENDING' || ride.status === 'MATCHED') && (
            <button
              onClick={handleDelete}
              disabled={deleteMut.isPending}
              className="p-2 text-destructive hover:bg-destructive/10 rounded-lg transition-colors border border-transparent hover:border-destructive/20"
              title="Cancel Ride"
            >
              {deleteMut.isPending ? <Loader2 className="h-5 w-5 animate-spin" /> : <Trash2 className="h-5 w-5" />}
            </button>
          )}
        </div>
      </div>

      <div className="bg-card border border-card-border rounded-xl p-6 shadow-sm">
        <h2 className="text-lg font-semibold mb-6">Status Timeline</h2>
        {isCancelled ? (
          <div className="flex items-center gap-3 text-destructive p-4 bg-destructive/10 rounded-lg">
            <AlertCircle className="h-5 w-5" />
            <span className="font-medium">This ride request has been cancelled.</span>
          </div>
        ) : (
          <div className="relative">
            <div className="absolute top-5 left-6 right-6 h-0.5 bg-secondary hidden md:block"></div>
            <div className="absolute top-5 left-6 h-0.5 bg-primary hidden md:block transition-all duration-500" 
                 style={{ width: `${(Math.max(0, currentStatusIndex) / (steps.length - 1)) * 100}%` }}></div>
            
            <div className="flex flex-col md:flex-row justify-between gap-6 relative z-10">
              {steps.map((step, idx) => {
                const isCompleted = currentStatusIndex >= idx;
                const isCurrent = currentStatusIndex === idx;
                
                return (
                  <div key={step.status} className="flex md:flex-col items-center gap-4 md:gap-3">
                    <div className={`h-10 w-10 rounded-full flex items-center justify-center shrink-0 border-2 transition-colors ${
                      isCompleted ? 'bg-primary border-primary text-primary-foreground' : 'bg-card border-secondary text-muted-foreground'
                    }`}>
                      <step.icon className="h-5 w-5" />
                    </div>
                    <div className="md:text-center">
                      <p className={`font-medium ${isCurrent ? 'text-foreground' : isCompleted ? 'text-foreground' : 'text-muted-foreground'}`}>
                        {step.label}
                      </p>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-card border border-card-border rounded-xl p-6 shadow-sm space-y-6">
          <h2 className="text-lg font-semibold flex items-center gap-2">
            <MapPin className="h-5 w-5 text-primary" /> Trip Route
          </h2>
          
          <div className="space-y-6 relative before:absolute before:inset-y-6 before:left-3.5 before:w-px before:bg-secondary">
            <div className="flex gap-4 relative z-10">
              <div className="h-7 w-7 rounded-full bg-secondary flex items-center justify-center shrink-0 ring-4 ring-card">
                <div className="h-2 w-2 rounded-full bg-muted-foreground"></div>
              </div>
              <div>
                <p className="text-sm font-medium text-muted-foreground mb-1">Pickup</p>
                <p className="text-foreground font-medium">{ride.pickupAddress}</p>
              </div>
            </div>
            
            <div className="flex gap-4 relative z-10">
              <div className="h-7 w-7 rounded-full bg-primary/20 flex items-center justify-center shrink-0 ring-4 ring-card">
                <div className="h-2 w-2 rounded-full bg-primary"></div>
              </div>
              <div>
                <p className="text-sm font-medium text-muted-foreground mb-1">Dropoff</p>
                <p className="text-foreground font-medium">{ride.destinationAddress}</p>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-card border border-card-border rounded-xl p-6 shadow-sm space-y-6">
          <h2 className="text-lg font-semibold flex items-center gap-2">
            <Navigation className="h-5 w-5 text-primary" /> Ride Info
          </h2>
          
          <div className="grid grid-cols-2 gap-4">
            <div className="p-3 bg-secondary/50 rounded-lg">
              <p className="text-xs font-medium text-muted-foreground mb-1">Passengers</p>
              <p className="font-semibold text-foreground flex items-center gap-1.5"><Users className="h-4 w-4" /> {ride.passengerCount || 1}</p>
            </div>
            <div className="p-3 bg-secondary/50 rounded-lg">
              <p className="text-xs font-medium text-muted-foreground mb-1">Type</p>
              <p className="font-semibold text-foreground capitalize">{ride.rideType?.replace('_', ' ').toLowerCase() || 'On Demand'}</p>
            </div>
            {ride.luggageSize && ride.luggageSize !== 'NONE' && (
              <div className="p-3 bg-secondary/50 rounded-lg col-span-2">
                <p className="text-xs font-medium text-muted-foreground mb-1">Luggage</p>
                <p className="font-semibold text-foreground capitalize">{ride.luggageSize.toLowerCase()}</p>
              </div>
            )}
            <div className="p-3 bg-secondary/50 rounded-lg col-span-2">
              <p className="text-xs font-medium text-muted-foreground mb-1">Requested At</p>
              <p className="font-semibold text-foreground">{ride.createdAt ? format(new Date(ride.createdAt), "PPP 'at' p") : "Unknown"}</p>
            </div>
            {ride.notes && (
              <div className="col-span-2 p-4 bg-amber-50/50 dark:bg-amber-900/10 border border-amber-100 dark:border-amber-900/30 rounded-lg mt-2">
                <p className="text-xs font-medium text-amber-800 dark:text-amber-500 mb-1 flex items-center gap-1.5">
                  <AlertCircle className="h-3 w-3" /> Driver Notes
                </p>
                <p className="text-sm text-amber-900 dark:text-amber-200">{ride.notes}</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
