import React from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { 
  useListPendingDrivers, 
  getListPendingDriversQueryKey,
  useApproveDriver,
  useRejectDriver
} from '@workspace/api-client-react';
import { 
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow 
} from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { Car, Check, X, FileText, AlertCircle } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';

export default function DriversPage() {
  const { toast } = useToast();
  const queryClient = useQueryClient();

  const { data: drivers, isLoading } = useListPendingDrivers({
    query: { queryKey: getListPendingDriversQueryKey() }
  });

  const approveMutation = useApproveDriver();
  const rejectMutation = useRejectDriver();

  const handleApprove = (id: string) => {
    approveMutation.mutate(
      { id },
      {
        onSuccess: () => {
          toast({ title: "Driver approved successfully" });
          queryClient.invalidateQueries({ queryKey: getListPendingDriversQueryKey() });
          // Also invalidate stats
          queryClient.invalidateQueries({ queryKey: ['/api/admin/stats'] });
        },
        onError: () => {
          toast({ title: "Error approving driver", variant: "destructive" });
        }
      }
    );
  };

  const handleReject = (id: string) => {
    rejectMutation.mutate(
      { id },
      {
        onSuccess: () => {
          toast({ title: "Driver application rejected" });
          queryClient.invalidateQueries({ queryKey: getListPendingDriversQueryKey() });
          queryClient.invalidateQueries({ queryKey: ['/api/admin/stats'] });
        },
        onError: () => {
          toast({ title: "Error rejecting driver", variant: "destructive" });
        }
      }
    );
  };

  return (
    <div className="p-8 max-w-[1600px] mx-auto space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Driver Applications</h1>
        <p className="text-muted-foreground">Review and approve pending driver requests.</p>
      </div>

      {!isLoading && drivers && drivers.length === 0 && (
        <Alert className="bg-muted/50">
          <Car className="h-4 w-4 text-muted-foreground" />
          <AlertTitle>All caught up!</AlertTitle>
          <AlertDescription>
            There are no pending driver applications to review at this time.
          </AlertDescription>
        </Alert>
      )}

      <div className="bg-card rounded-lg border shadow-sm overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Applicant</TableHead>
              <TableHead>Contact</TableHead>
              <TableHead>License Info</TableHead>
              <TableHead>Vehicle Info</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              Array.from({ length: 3 }).map((_, i) => (
                <TableRow key={i}>
                  <TableCell><Skeleton className="h-10 w-[150px]" /></TableCell>
                  <TableCell><Skeleton className="h-10 w-[150px]" /></TableCell>
                  <TableCell><Skeleton className="h-8 w-[120px]" /></TableCell>
                  <TableCell><Skeleton className="h-8 w-[120px]" /></TableCell>
                  <TableCell><Skeleton className="h-8 w-[120px] ml-auto" /></TableCell>
                </TableRow>
              ))
            ) : drivers && drivers.length > 0 ? (
              drivers.map((driver) => (
                <TableRow key={driver.id}>
                  <TableCell>
                    <div className="flex flex-col">
                      <span className="font-medium">{driver.fullName}</span>
                      <span className="text-xs text-muted-foreground font-mono truncate max-w-[150px]">{driver.id}</span>
                    </div>
                  </TableCell>
                  <TableCell>
                    <div className="flex flex-col text-sm">
                      <span>{driver.email}</span>
                      {driver.phone && <span className="text-muted-foreground">{driver.phone}</span>}
                    </div>
                  </TableCell>
                  <TableCell>
                    {driver.licenseNumber ? (
                      <div className="flex items-center gap-2">
                        <FileText className="w-4 h-4 text-muted-foreground" />
                        <span className="font-mono text-sm">{driver.licenseNumber}</span>
                      </div>
                    ) : (
                      <span className="text-xs text-destructive flex items-center gap-1">
                        <AlertCircle className="w-3 h-3" /> Missing
                      </span>
                    )}
                  </TableCell>
                  <TableCell>
                    {driver.vehiclePlate ? (
                      <div className="flex items-center gap-2">
                        <Car className="w-4 h-4 text-muted-foreground" />
                        <span className="font-mono text-sm bg-muted px-2 py-0.5 rounded border">{driver.vehiclePlate}</span>
                      </div>
                    ) : (
                      <span className="text-xs text-destructive flex items-center gap-1">
                        <AlertCircle className="w-3 h-3" /> Missing
                      </span>
                    )}
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      <Button 
                        variant="outline" 
                        size="sm" 
                        className="text-destructive hover:bg-destructive hover:text-destructive-foreground"
                        onClick={() => handleReject(driver.id)}
                        disabled={rejectMutation.isPending || approveMutation.isPending}
                      >
                        <X className="w-4 h-4 mr-1" /> Reject
                      </Button>
                      <Button 
                        size="sm" 
                        className="bg-primary text-primary-foreground hover:bg-primary/90"
                        onClick={() => handleApprove(driver.id)}
                        disabled={approveMutation.isPending || rejectMutation.isPending}
                      >
                        <Check className="w-4 h-4 mr-1" /> Approve
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))
            ) : null}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}