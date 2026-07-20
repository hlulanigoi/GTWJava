import * as React from "react";
import { useGetMyRides } from "@workspace/api-client-react";
import { Link } from "wouter";
import { MapPin, Plus, Loader2, ChevronRight, Users, Navigation } from "lucide-react";
import { format } from "date-fns";
import { Badge } from "@/components/ui/badge";

export default function RidesList() {
  const { data: rides, isLoading } = useGetMyRides();
  const [filter, setFilter] = React.useState<string>("ALL");

  if (isLoading) {
    return (
      <div className="flex-1 p-8 flex items-center justify-center min-h-[50vh]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  const tabs = ["ALL", "PENDING", "MATCHED", "EN_ROUTE", "COMPLETED", "CANCELLED"];
  
  const filteredRides = rides?.filter(r => filter === "ALL" || r.status === filter) || [];

  return (
    <div className="p-6 md:p-8 max-w-6xl mx-auto space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-foreground">My Rides</h1>
          <p className="text-muted-foreground text-sm mt-1">Track your booked rides.</p>
        </div>
        <Link href="/rides/new" className="inline-flex items-center justify-center gap-2 px-4 py-2 bg-primary text-primary-foreground rounded-lg font-medium hover:bg-primary/90 transition-colors shadow-sm whitespace-nowrap">
          <Plus className="h-4 w-4" /> Book Ride
        </Link>
      </div>

      <div className="flex overflow-x-auto pb-2 -mx-2 px-2 gap-2 hide-scrollbar">
        {tabs.map(tab => (
          <button
            key={tab}
            onClick={() => setFilter(tab)}
            className={`px-4 py-1.5 rounded-full text-sm font-medium transition-colors whitespace-nowrap ${
              filter === tab 
                ? "bg-foreground text-background" 
                : "bg-secondary text-secondary-foreground hover:bg-secondary/80"
            }`}
          >
            {tab}
          </button>
        ))}
      </div>

      {filteredRides.length === 0 ? (
        <div className="bg-card border border-card-border rounded-xl p-12 text-center flex flex-col items-center">
          <div className="h-16 w-16 bg-secondary rounded-full flex items-center justify-center mb-4">
            <Navigation className="h-8 w-8 text-muted-foreground" />
          </div>
          <h3 className="text-lg font-semibold text-foreground">No rides found</h3>
          <p className="text-muted-foreground mt-1 max-w-sm">
            {filter === "ALL" 
              ? "You haven't requested any rides yet." 
              : `You don't have any rides with status ${filter}.`}
          </p>
          {filter === "ALL" && (
            <Link href="/rides/new" className="mt-6 text-primary font-medium hover:underline">
              Request your first ride
            </Link>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          {filteredRides.map(ride => (
            <Link key={ride.id} href={`/rides/${ride.id}`} className="bg-card hover:bg-muted/30 border border-card-border rounded-xl p-5 transition-colors shadow-sm flex flex-col group block">
              <div className="flex justify-between items-start mb-5">
                <div className="flex-1 pr-4">
                  <h3 className="font-semibold text-lg text-foreground line-clamp-1">{ride.destinationAddress}</h3>
                  <p className="text-sm text-muted-foreground mt-1">
                    {ride.createdAt ? format(new Date(ride.createdAt), "MMM d, yyyy") : ""}
                    {ride.rideType === 'SCHEDULED' && " • Scheduled"}
                  </p>
                </div>
                <Badge status={ride.status} />
              </div>
              
              <div className="space-y-4 mb-5">
                <div className="flex gap-3">
                  <div className="flex flex-col items-center mt-1">
                    <div className="h-2.5 w-2.5 rounded-full bg-secondary ring-2 ring-background z-10"></div>
                    <div className="w-px h-8 bg-border my-0.5"></div>
                    <div className="h-2.5 w-2.5 rounded-full bg-primary ring-2 ring-background z-10"></div>
                  </div>
                  <div className="flex-1 space-y-3">
                    <div>
                      <p className="text-xs font-medium text-muted-foreground">Pickup</p>
                      <p className="text-sm font-medium text-foreground line-clamp-1">{ride.pickupAddress}</p>
                    </div>
                    <div>
                      <p className="text-xs font-medium text-muted-foreground">Dropoff</p>
                      <p className="text-sm font-medium text-foreground line-clamp-1">{ride.destinationAddress}</p>
                    </div>
                  </div>
                </div>
              </div>
              
              <div className="mt-auto pt-4 border-t border-card-border flex items-center justify-between">
                <div className="flex items-center gap-4 text-sm text-muted-foreground">
                  <span className="flex items-center gap-1.5"><Users className="h-4 w-4" /> {ride.passengerCount || 1} pass.</span>
                  {ride.luggageSize && ride.luggageSize !== 'NONE' && (
                    <span className="capitalize px-2 py-0.5 bg-secondary rounded text-xs font-medium">{ride.luggageSize.toLowerCase()} luggage</span>
                  )}
                </div>
                <div className="flex items-center text-primary text-sm font-medium opacity-0 group-hover:opacity-100 transition-opacity">
                  Details <ChevronRight className="h-4 w-4 ml-1" />
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
