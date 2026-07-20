import * as React from "react";
import { useGetMe, useGetMyParcels, useGetMyRides } from "@workspace/api-client-react";
import { Link } from "wouter";
import { Package, MapPin, ArrowRight, Loader2, Clock } from "lucide-react";
import { format } from "date-fns";

export default function Dashboard() {
  const { data: user } = useGetMe();
  const { data: parcels, isLoading: loadingParcels } = useGetMyParcels();
  const { data: rides, isLoading: loadingRides } = useGetMyRides();

  if (loadingParcels || loadingRides) {
    return (
      <div className="flex-1 p-8 flex items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  const activeParcels = parcels?.filter(p => p.status === 'PENDING' || p.status === 'MATCHED' || p.status === 'COLLECTED') || [];
  const activeRides = rides?.filter(r => r.status === 'PENDING' || r.status === 'MATCHED' || r.status === 'EN_ROUTE') || [];

  return (
    <div className="p-6 md:p-8 max-w-6xl mx-auto space-y-8 animate-in fade-in duration-500">
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Welcome, {user?.fullName?.split(" ")[0] || "User"}</h1>
          <p className="text-muted-foreground mt-1">Here's what's happening with your requests today.</p>
        </div>
        <div className="flex items-center gap-3">
          <Link href="/parcels/new" className="px-4 py-2 bg-primary text-primary-foreground font-medium rounded-lg hover:bg-primary/90 transition-colors shadow-sm text-sm">
            Send Parcel
          </Link>
          <Link href="/rides/new" className="px-4 py-2 bg-secondary text-secondary-foreground font-medium rounded-lg hover:bg-secondary/80 transition-colors shadow-sm text-sm">
            Book Ride
          </Link>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard title="Active Parcels" value={activeParcels.length} icon={Package} color="text-blue-500" bg="bg-blue-50 dark:bg-blue-500/10" />
        <StatCard title="Active Rides" value={activeRides.length} icon={MapPin} color="text-purple-500" bg="bg-purple-50 dark:bg-purple-500/10" />
        <StatCard title="Total Sent" value={parcels?.filter(p => p.status === 'DELIVERED').length || 0} icon={Package} color="text-green-500" bg="bg-green-50 dark:bg-green-500/10" />
        <StatCard title="Rides Taken" value={user?.totalRidesTaken || 0} icon={MapPin} color="text-amber-500" bg="bg-amber-50 dark:bg-amber-500/10" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div className="bg-card border border-card-border rounded-xl shadow-sm overflow-hidden flex flex-col">
          <div className="p-5 border-b border-card-border flex items-center justify-between">
            <h2 className="font-semibold text-lg flex items-center gap-2">
              <Package className="h-5 w-5 text-primary" /> Recent Parcels
            </h2>
            <Link href="/parcels" className="text-sm text-primary font-medium hover:underline flex items-center gap-1">
              View all <ArrowRight className="h-4 w-4" />
            </Link>
          </div>
          <div className="flex-1 p-0">
            {parcels && parcels.length > 0 ? (
              <ul className="divide-y divide-card-border">
                {parcels.slice(0, 4).map(parcel => (
                  <li key={parcel.id} className="p-5 hover:bg-muted/50 transition-colors">
                    <Link href={`/parcels/${parcel.id}`} className="flex items-start justify-between">
                      <div>
                        <p className="font-medium text-foreground line-clamp-1">{parcel.description}</p>
                        <p className="text-sm text-muted-foreground mt-1 flex items-center gap-1">
                          <Clock className="h-3 w-3" />
                          {parcel.createdAt ? format(new Date(parcel.createdAt), "MMM d, h:mm a") : ""}
                        </p>
                      </div>
                      <Badge status={parcel.status} />
                    </Link>
                  </li>
                ))}
              </ul>
            ) : (
              <div className="p-8 text-center text-muted-foreground flex flex-col items-center">
                <Package className="h-8 w-8 mb-2 opacity-20" />
                <p>No parcels yet</p>
                <Link href="/parcels/new" className="text-primary text-sm mt-2 font-medium hover:underline">Create your first parcel</Link>
              </div>
            )}
          </div>
        </div>

        <div className="bg-card border border-card-border rounded-xl shadow-sm overflow-hidden flex flex-col">
          <div className="p-5 border-b border-card-border flex items-center justify-between">
            <h2 className="font-semibold text-lg flex items-center gap-2">
              <MapPin className="h-5 w-5 text-primary" /> Recent Rides
            </h2>
            <Link href="/rides" className="text-sm text-primary font-medium hover:underline flex items-center gap-1">
              View all <ArrowRight className="h-4 w-4" />
            </Link>
          </div>
          <div className="flex-1 p-0">
            {rides && rides.length > 0 ? (
              <ul className="divide-y divide-card-border">
                {rides.slice(0, 4).map(ride => (
                  <li key={ride.id} className="p-5 hover:bg-muted/50 transition-colors">
                    <Link href={`/rides/${ride.id}`} className="flex items-start justify-between">
                      <div className="max-w-[70%]">
                        <p className="font-medium text-foreground truncate">{ride.destinationAddress}</p>
                        <p className="text-sm text-muted-foreground mt-1 truncate flex items-center gap-1">
                          From: {ride.pickupAddress}
                        </p>
                      </div>
                      <Badge status={ride.status} />
                    </Link>
                  </li>
                ))}
              </ul>
            ) : (
              <div className="p-8 text-center text-muted-foreground flex flex-col items-center">
                <MapPin className="h-8 w-8 mb-2 opacity-20" />
                <p>No rides requested</p>
                <Link href="/rides/new" className="text-primary text-sm mt-2 font-medium hover:underline">Request a ride</Link>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

function StatCard({ title, value, icon: Icon, color, bg }: { title: string, value: number, icon: any, color: string, bg: string }) {
  return (
    <div className="bg-card border border-card-border rounded-xl p-5 shadow-sm flex items-center gap-4">
      <div className={`h-12 w-12 rounded-full flex items-center justify-center ${bg} ${color}`}>
        <Icon className="h-6 w-6" />
      </div>
      <div>
        <p className="text-sm font-medium text-muted-foreground">{title}</p>
        <p className="text-2xl font-bold text-foreground">{value}</p>
      </div>
    </div>
  );
}

export function Badge({ status }: { status: string }) {
  const styles: Record<string, string> = {
    PENDING: "bg-amber-100 text-amber-800 border-amber-200 dark:bg-amber-900/30 dark:text-amber-300 dark:border-amber-800",
    MATCHED: "bg-blue-100 text-blue-800 border-blue-200 dark:bg-blue-900/30 dark:text-blue-300 dark:border-blue-800",
    EN_ROUTE: "bg-purple-100 text-purple-800 border-purple-200 dark:bg-purple-900/30 dark:text-purple-300 dark:border-purple-800",
    COLLECTED: "bg-purple-100 text-purple-800 border-purple-200 dark:bg-purple-900/30 dark:text-purple-300 dark:border-purple-800",
    DELIVERED: "bg-green-100 text-green-800 border-green-200 dark:bg-green-900/30 dark:text-green-300 dark:border-green-800",
    COMPLETED: "bg-green-100 text-green-800 border-green-200 dark:bg-green-900/30 dark:text-green-300 dark:border-green-800",
    ACTIVE: "bg-green-100 text-green-800 border-green-200 dark:bg-green-900/30 dark:text-green-300 dark:border-green-800",
    CANCELLED: "bg-red-100 text-red-800 border-red-200 dark:bg-red-900/30 dark:text-red-300 dark:border-red-800",
    USED: "bg-gray-100 text-gray-800 border-gray-200 dark:bg-gray-800 dark:text-gray-300 dark:border-gray-700",
    EXPIRED: "bg-red-100 text-red-800 border-red-200 dark:bg-red-900/30 dark:text-red-300 dark:border-red-800",
  };
  
  const style = styles[status] || "bg-gray-100 text-gray-800 border-gray-200";
  
  return (
    <span className={`px-2.5 py-0.5 rounded-full text-xs font-semibold border ${style}`}>
      {status.replace("_", " ")}
    </span>
  );
}
