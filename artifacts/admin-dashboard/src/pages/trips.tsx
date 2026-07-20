import React, { useState } from 'react';
import { 
  useListAdminTrips, 
  getListAdminTripsQueryKey 
} from '@workspace/api-client-react';
import { 
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow 
} from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { 
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue 
} from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import { format } from 'date-fns';
import { MapPin, Navigation, Truck, Train, Plane, Ship, Bus } from 'lucide-react';

const getStatusBadge = (status: string) => {
  switch(status) {
    case 'SCHEDULED': return <Badge variant="outline" className="bg-blue-50 text-blue-600 border-blue-200">Scheduled</Badge>;
    case 'ACTIVE': return <Badge variant="outline" className="bg-primary/10 text-primary border-primary/20">Active</Badge>;
    case 'COMPLETED': return <Badge variant="outline" className="bg-emerald-50 text-emerald-600 border-emerald-200">Completed</Badge>;
    case 'CANCELLED': return <Badge variant="secondary">Cancelled</Badge>;
    default: return <Badge variant="outline">{status}</Badge>;
  }
};

const getTransportIcon = (mode: string) => {
  switch(mode?.toUpperCase()) {
    case 'TRUCK': return <Truck className="w-4 h-4" />;
    case 'TRAIN': return <Train className="w-4 h-4" />;
    case 'PLANE': return <Plane className="w-4 h-4" />;
    case 'SHIP': return <Ship className="w-4 h-4" />;
    case 'BUS': return <Bus className="w-4 h-4" />;
    default: return <Truck className="w-4 h-4" />;
  }
};

export default function TripsPage() {
  const [page, setPage] = useState(1);
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  const { data, isLoading } = useListAdminTrips(
    { page, status: statusFilter !== 'ALL' ? statusFilter : undefined },
    { 
      query: { 
        queryKey: getListAdminTripsQueryKey({ page, status: statusFilter !== 'ALL' ? statusFilter : undefined }) 
      } 
    }
  );

  return (
    <div className="p-8 max-w-[1600px] mx-auto space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Trips</h1>
          <p className="text-muted-foreground">Monitor carrier trips and corridors.</p>
        </div>
      </div>

      <div className="flex bg-card p-4 rounded-lg border shadow-sm">
        <div className="flex items-center gap-2 w-full sm:w-auto">
          <span className="text-sm font-medium text-muted-foreground">Filter Status:</span>
          <Select 
            value={statusFilter} 
            onValueChange={(val) => { setStatusFilter(val); setPage(1); }}
          >
            <SelectTrigger className="w-[180px] bg-background">
              <SelectValue placeholder="All Statuses" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">All Statuses</SelectItem>
              <SelectItem value="SCHEDULED">Scheduled</SelectItem>
              <SelectItem value="ACTIVE">Active</SelectItem>
              <SelectItem value="COMPLETED">Completed</SelectItem>
              <SelectItem value="CANCELLED">Cancelled</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      <div className="bg-card rounded-lg border shadow-sm overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Corridor</TableHead>
              <TableHead>Mode & Capacity</TableHead>
              <TableHead>Departure</TableHead>
              <TableHead>Carrier ID</TableHead>
              <TableHead>Status</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              Array.from({ length: 5 }).map((_, i) => (
                <TableRow key={i}>
                  <TableCell><Skeleton className="h-12 w-[250px]" /></TableCell>
                  <TableCell><Skeleton className="h-8 w-[100px]" /></TableCell>
                  <TableCell><Skeleton className="h-6 w-[120px]" /></TableCell>
                  <TableCell><Skeleton className="h-6 w-[100px]" /></TableCell>
                  <TableCell><Skeleton className="h-6 w-[80px]" /></TableCell>
                </TableRow>
              ))
            ) : !data || data.data.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="text-center py-10 text-muted-foreground">
                  No trips found.
                </TableCell>
              </TableRow>
            ) : (
              data.data.map((trip) => (
                <TableRow key={trip.id}>
                  <TableCell>
                    <div className="flex flex-col gap-1.5 max-w-[350px]">
                      <div className="flex items-start gap-2">
                        <MapPin className="w-3.5 h-3.5 mt-0.5 text-muted-foreground shrink-0" />
                        <span className="text-sm leading-tight truncate" title={trip.originAddress || 'Unknown'}>{trip.originAddress || 'Unknown'}</span>
                      </div>
                      <div className="flex items-start gap-2">
                        <Navigation className="w-3.5 h-3.5 mt-0.5 text-primary shrink-0" />
                        <span className="text-sm font-medium leading-tight truncate" title={trip.destAddress || 'Unknown'}>{trip.destAddress || 'Unknown'}</span>
                      </div>
                    </div>
                  </TableCell>
                  <TableCell>
                    <div className="flex flex-col gap-1">
                      <div className="flex items-center gap-1.5 text-sm font-medium capitalize">
                        {getTransportIcon(trip.transportMode || 'truck')}
                        {trip.transportMode || 'Truck'}
                      </div>
                      <span className="text-xs text-muted-foreground">Capacity: <span className="font-mono">{trip.capacity || 0}</span></span>
                    </div>
                  </TableCell>
                  <TableCell>
                    <div className="text-sm font-medium">
                      {trip.departureTime ? format(new Date(trip.departureTime), 'MMM d, yyyy HH:mm') : 'Unknown'}
                    </div>
                    <div className="text-xs text-muted-foreground mt-1 font-mono">
                      Trip ID: {trip.id.substring(0,8)}...
                    </div>
                  </TableCell>
                  <TableCell>
                    <span className="font-mono text-sm text-muted-foreground">
                      {trip.carrierId ? trip.carrierId.substring(0,12) + '...' : '-'}
                    </span>
                  </TableCell>
                  <TableCell>
                    {getStatusBadge(trip.status)}
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {data && data.total > data.pageSize && (
        <div className="flex justify-between items-center bg-card p-4 rounded-lg border shadow-sm">
          <div className="text-sm text-muted-foreground">
            Showing <span className="font-medium text-foreground">{(page - 1) * data.pageSize + 1}</span> to <span className="font-medium text-foreground">{Math.min(page * data.pageSize, data.total)}</span> of <span className="font-medium text-foreground">{data.total}</span>
          </div>
          <div className="flex gap-2">
            <Button 
              variant="outline" 
              size="sm" 
              onClick={() => setPage(p => Math.max(1, p - 1))}
              disabled={page === 1}
            >
              Previous
            </Button>
            <Button 
              variant="outline" 
              size="sm" 
              onClick={() => setPage(p => p + 1)}
              disabled={page * data.pageSize >= data.total}
            >
              Next
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}