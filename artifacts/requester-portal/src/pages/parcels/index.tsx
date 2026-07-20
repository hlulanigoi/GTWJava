import * as React from "react";
import { useGetMyParcels } from "@workspace/api-client-react";
import { Link } from "wouter";
import { Package, MapPin, Plus, Loader2, ChevronRight, Weight } from "lucide-react";
import { format } from "date-fns";
import { Badge } from "@/components/ui/badge";

export default function ParcelsList() {
  const { data: parcels, isLoading } = useGetMyParcels();
  const [filter, setFilter] = React.useState<string>("ALL");

  if (isLoading) {
    return (
      <div className="flex-1 p-8 flex items-center justify-center min-h-[50vh]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  const tabs = ["ALL", "PENDING", "MATCHED", "COLLECTED", "DELIVERED", "CANCELLED"];
  
  const filteredParcels = parcels?.filter(p => filter === "ALL" || p.status === filter) || [];

  return (
    <div className="p-6 md:p-8 max-w-6xl mx-auto space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-foreground">My Parcels</h1>
          <p className="text-muted-foreground text-sm mt-1">Track and manage your sent items.</p>
        </div>
        <Link href="/parcels/new" className="inline-flex items-center justify-center gap-2 px-4 py-2 bg-primary text-primary-foreground rounded-lg font-medium hover:bg-primary/90 transition-colors shadow-sm whitespace-nowrap">
          <Plus className="h-4 w-4" /> New Parcel
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

      {filteredParcels.length === 0 ? (
        <div className="bg-card border border-card-border rounded-xl p-12 text-center flex flex-col items-center">
          <div className="h-16 w-16 bg-secondary rounded-full flex items-center justify-center mb-4">
            <Package className="h-8 w-8 text-muted-foreground" />
          </div>
          <h3 className="text-lg font-semibold text-foreground">No parcels found</h3>
          <p className="text-muted-foreground mt-1 max-w-sm">
            {filter === "ALL" 
              ? "You haven't sent any parcels yet." 
              : `You don't have any parcels with status ${filter}.`}
          </p>
          {filter === "ALL" && (
            <Link href="/parcels/new" className="mt-6 text-primary font-medium hover:underline">
              Create your first parcel
            </Link>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {filteredParcels.map(parcel => (
            <Link key={parcel.id} href={`/parcels/${parcel.id}`} className="bg-card hover:bg-muted/30 border border-card-border rounded-xl p-5 transition-colors shadow-sm flex flex-col group block">
              <div className="flex justify-between items-start mb-4">
                <div className="flex-1 pr-4">
                  <h3 className="font-semibold text-lg text-foreground line-clamp-1">{parcel.description}</h3>
                  <p className="text-sm text-muted-foreground mt-1">
                    {parcel.createdAt ? format(new Date(parcel.createdAt), "MMM d, yyyy") : ""}
                  </p>
                </div>
                <Badge status={parcel.status} />
              </div>
              
              <div className="space-y-3 mt-auto">
                <div className="flex items-start gap-3">
                  <MapPin className="h-4 w-4 text-muted-foreground mt-0.5 shrink-0" />
                  <div className="text-sm">
                    <p className="text-muted-foreground text-xs font-medium">From</p>
                    <p className="text-foreground line-clamp-1">{parcel.pickupAddress}</p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <MapPin className="h-4 w-4 text-primary mt-0.5 shrink-0" />
                  <div className="text-sm">
                    <p className="text-muted-foreground text-xs font-medium">To</p>
                    <p className="text-foreground line-clamp-1">{parcel.destinationAddress}</p>
                  </div>
                </div>
              </div>
              
              <div className="mt-5 pt-4 border-t border-card-border flex items-center justify-between">
                <div className="flex items-center gap-4 text-sm text-muted-foreground">
                  <span className="flex items-center gap-1.5"><Weight className="h-4 w-4" /> {parcel.weight} kg</span>
                  <span className="capitalize px-2 py-0.5 bg-secondary rounded text-xs font-medium">{parcel.size?.toLowerCase()}</span>
                </div>
                <div className="flex items-center text-primary text-sm font-medium opacity-0 group-hover:opacity-100 transition-opacity">
                  View details <ChevronRight className="h-4 w-4 ml-1" />
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
