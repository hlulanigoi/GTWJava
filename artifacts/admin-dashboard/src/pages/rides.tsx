import React, { useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { 
  useListAdminRides, 
  getListAdminRidesQueryKey,
  useUpdateAdminRideStatus
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
import { useToast } from '@/hooks/use-toast';
import { MapPin, Navigation } from 'lucide-react';

const getStatusBadge = (status: string) => {
  switch(status) {
    case 'PENDING': return <Badge variant="outline" className="bg-amber-50 text-amber-600 border-amber-200">Pending</Badge>;
    case 'ACCEPTED': return <Badge variant="outline" className="bg-blue-50 text-blue-600 border-blue-200">Accepted</Badge>;
    case 'IN_PROGRESS': return <Badge variant="outline" className="bg-primary/10 text-primary border-primary/20">In Progress</Badge>;
    case 'COMPLETED': return <Badge variant="outline" className="bg-emerald-50 text-emerald-600 border-emerald-200">Completed</Badge>;
    case 'CANCELLED': return <Badge variant="secondary">Cancelled</Badge>;
    default: return <Badge variant="outline">{status}</Badge>;
  }
};

export default function RidesPage() {
  const [page, setPage] = useState(1);
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const { toast } = useToast();
  const queryClient = useQueryClient();

  const { data, isLoading } = useListAdminRides(
    { page, status: statusFilter !== 'ALL' ? statusFilter : undefined },
    { 
      query: { 
        queryKey: getListAdminRidesQueryKey({ page, status: statusFilter !== 'ALL' ? statusFilter : undefined }) 
      } 
    }
  );

  const updateStatusMutation = useUpdateAdminRideStatus();

  const handleStatusChange = (id: string, newStatus: string) => {
    updateStatusMutation.mutate(
      { id, data: { status: newStatus } },
      {
        onSuccess: () => {
          toast({ title: "Ride status updated" });
          queryClient.invalidateQueries({ queryKey: ['/api/admin/rides'] });
        },
        onError: () => {
          toast({ title: "Error updating status", variant: "destructive" });
        }
      }
    );
  };

  return (
    <div className="p-8 max-w-[1600px] mx-auto space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Rides</h1>
          <p className="text-muted-foreground">Monitor platform ride requests.</p>
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
              <SelectItem value="PENDING">Pending</SelectItem>
              <SelectItem value="ACCEPTED">Accepted</SelectItem>
              <SelectItem value="IN_PROGRESS">In Progress</SelectItem>
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
              <TableHead>Route</TableHead>
              <TableHead>IDs</TableHead>
              <TableHead>Schedule</TableHead>
              <TableHead>Fare</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Action</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              Array.from({ length: 5 }).map((_, i) => (
                <TableRow key={i}>
                  <TableCell><Skeleton className="h-12 w-[250px]" /></TableCell>
                  <TableCell><Skeleton className="h-8 w-[100px]" /></TableCell>
                  <TableCell><Skeleton className="h-6 w-[120px]" /></TableCell>
                  <TableCell><Skeleton className="h-6 w-[60px]" /></TableCell>
                  <TableCell><Skeleton className="h-6 w-[80px]" /></TableCell>
                  <TableCell><Skeleton className="h-8 w-[120px] ml-auto" /></TableCell>
                </TableRow>
              ))
            ) : !data || data.data.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="text-center py-10 text-muted-foreground">
                  No rides found.
                </TableCell>
              </TableRow>
            ) : (
              data.data.map((ride) => (
                <TableRow key={ride.id}>
                  <TableCell>
                    <div className="flex flex-col gap-1.5 max-w-[300px]">
                      <div className="flex items-start gap-2">
                        <MapPin className="w-3.5 h-3.5 mt-0.5 text-muted-foreground shrink-0" />
                        <span className="text-sm leading-tight truncate" title={ride.pickupAddress || 'Unknown'}>{ride.pickupAddress || 'Unknown'}</span>
                      </div>
                      <div className="flex items-start gap-2">
                        <Navigation className="w-3.5 h-3.5 mt-0.5 text-primary shrink-0" />
                        <span className="text-sm font-medium leading-tight truncate" title={ride.destinationAddress || 'Unknown'}>{ride.destinationAddress || 'Unknown'}</span>
                      </div>
                    </div>
                  </TableCell>
                  <TableCell>
                    <div className="flex flex-col text-xs font-mono text-muted-foreground space-y-1">
                      <span title="Ride ID">R: {ride.id.substring(0,8)}...</span>
                      {ride.riderId && <span title="Rider ID">P: {ride.riderId.substring(0,8)}...</span>}
                      {ride.driverId && <span title="Driver ID" className="text-primary/80">D: {ride.driverId.substring(0,8)}...</span>}
                    </div>
                  </TableCell>
                  <TableCell>
                    <div className="text-sm">
                      {ride.scheduledAt ? (
                        format(new Date(ride.scheduledAt), 'MMM d, yyyy HH:mm')
                      ) : (
                        <span className="text-muted-foreground">Immediate</span>
                      )}
                    </div>
                    <div className="text-xs text-muted-foreground mt-1">
                      Req: {ride.createdAt ? format(new Date(ride.createdAt), 'MMM d') : 'Unknown'}
                    </div>
                  </TableCell>
                  <TableCell>
                    {ride.fare ? (
                      <span className="font-mono font-medium">${(parseInt(ride.fare) / 100).toFixed(2)}</span>
                    ) : (
                      <span className="text-muted-foreground">-</span>
                    )}
                  </TableCell>
                  <TableCell>
                    {getStatusBadge(ride.status)}
                  </TableCell>
                  <TableCell className="text-right">
                    <Select 
                      value={ride.status} 
                      onValueChange={(val) => handleStatusChange(ride.id, val)}
                    >
                      <SelectTrigger className="w-[140px] h-8 text-xs ml-auto">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="PENDING">PENDING</SelectItem>
                        <SelectItem value="ACCEPTED">ACCEPTED</SelectItem>
                        <SelectItem value="IN_PROGRESS">IN_PROGRESS</SelectItem>
                        <SelectItem value="COMPLETED">COMPLETED</SelectItem>
                        <SelectItem value="CANCELLED">CANCELLED</SelectItem>
                      </SelectContent>
                    </Select>
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