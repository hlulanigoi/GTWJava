import React, { useState, useRef } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { 
  useListAdminUsers, 
  getListAdminUsersQueryKey,
  useActivateUser,
  useDeactivateUser,
  useUpdateUserRole
} from '@workspace/api-client-react';
import { 
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow 
} from '@/components/ui/table';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { 
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue 
} from '@/components/ui/select';
import { Switch } from '@/components/ui/switch';
import { Search, Shield, User as UserIcon } from 'lucide-react';
import { format } from 'date-fns';
import { useToast } from '@/hooks/use-toast';

export default function UsersPage() {
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [role, setRole] = useState<string>('ALL');
  const { toast } = useToast();
  const queryClient = useQueryClient();

  const { data, isLoading } = useListAdminUsers(
    { 
      page, 
      search: search || undefined, 
      role: role !== 'ALL' ? (role as any) : undefined 
    },
    { 
      query: { 
        queryKey: getListAdminUsersQueryKey({ 
          page, 
          search: search || undefined, 
          role: role !== 'ALL' ? (role as any) : undefined 
        }) 
      } 
    }
  );

  const activateMutation = useActivateUser();
  const deactivateMutation = useDeactivateUser();
  const updateRoleMutation = useUpdateUserRole();

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setSearch(searchInput);
    setPage(1);
  };

  const handleToggleActive = (id: string, currentlyActive: boolean) => {
    const mutation = currentlyActive ? deactivateMutation : activateMutation;
    mutation.mutate(
      { id },
      {
        onSuccess: () => {
          toast({
            title: `User ${currentlyActive ? 'deactivated' : 'activated'}`,
          });
          queryClient.invalidateQueries({ queryKey: ['/api/admin/users'] });
        },
        onError: () => {
          toast({
            title: "Error updating user",
            variant: "destructive"
          });
        }
      }
    );
  };

  const handleRoleChange = (id: string, newRole: string) => {
    updateRoleMutation.mutate(
      { id, data: { role: newRole as any } },
      {
        onSuccess: () => {
          toast({ title: "Role updated" });
          queryClient.invalidateQueries({ queryKey: ['/api/admin/users'] });
        },
        onError: () => {
          toast({ title: "Error updating role", variant: "destructive" });
        }
      }
    );
  };

  return (
    <div className="p-8 max-w-[1600px] mx-auto space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Users</h1>
          <p className="text-muted-foreground">Manage user accounts and roles.</p>
        </div>
      </div>

      <div className="flex flex-col sm:flex-row gap-4 mb-6 bg-card p-4 rounded-lg border shadow-sm">
        <form onSubmit={handleSearch} className="flex-1 flex gap-2">
          <div className="relative flex-1 max-w-sm">
            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search users..."
              className="pl-9 bg-background"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
            />
          </div>
          <Button type="submit" variant="secondary">Search</Button>
        </form>
        
        <Select 
          value={role} 
          onValueChange={(val) => { setRole(val); setPage(1); }}
        >
          <SelectTrigger className="w-[180px] bg-background">
            <SelectValue placeholder="All Roles" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">All Roles</SelectItem>
            <SelectItem value="USER">Users</SelectItem>
            <SelectItem value="ADMIN">Admins</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <div className="bg-card rounded-lg border shadow-sm overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>User</TableHead>
              <TableHead>Role</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Stats</TableHead>
              <TableHead className="text-right">Joined</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              Array.from({ length: 5 }).map((_, i) => (
                <TableRow key={i}>
                  <TableCell><Skeleton className="h-10 w-[200px]" /></TableCell>
                  <TableCell><Skeleton className="h-8 w-[100px]" /></TableCell>
                  <TableCell><Skeleton className="h-6 w-[60px]" /></TableCell>
                  <TableCell><Skeleton className="h-6 w-[80px] ml-auto" /></TableCell>
                  <TableCell><Skeleton className="h-6 w-[80px] ml-auto" /></TableCell>
                </TableRow>
              ))
            ) : !data || data.data.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="text-center py-10 text-muted-foreground">
                  No users found.
                </TableCell>
              </TableRow>
            ) : (
              data.data.map((user) => (
                <TableRow key={user.id}>
                  <TableCell>
                    <div className="flex flex-col">
                      <span className="font-medium">{user.fullName}</span>
                      <span className="text-xs text-muted-foreground font-mono">{user.email}</span>
                      {user.phone && <span className="text-xs text-muted-foreground">{user.phone}</span>}
                    </div>
                  </TableCell>
                  <TableCell>
                    <Select 
                      value={user.role} 
                      onValueChange={(val) => handleRoleChange(user.id, val)}
                    >
                      <SelectTrigger className="w-[110px] h-8 text-xs">
                        <div className="flex items-center gap-2">
                          {user.role === 'ADMIN' ? <Shield className="w-3 h-3 text-primary" /> : <UserIcon className="w-3 h-3" />}
                          <SelectValue />
                        </div>
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="USER">USER</SelectItem>
                        <SelectItem value="ADMIN">ADMIN</SelectItem>
                      </SelectContent>
                    </Select>
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      <Switch 
                        checked={user.isActive} 
                        onCheckedChange={() => handleToggleActive(user.id, user.isActive)}
                      />
                      <Badge variant={user.isActive ? "outline" : "secondary"} className={user.isActive ? "text-emerald-600 border-emerald-200 bg-emerald-50" : ""}>
                        {user.isActive ? 'Active' : 'Inactive'}
                      </Badge>
                    </div>
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex flex-col items-end gap-1 text-xs">
                      {user.isApprovedDriver && <Badge className="bg-primary hover:bg-primary/90 text-primary-foreground font-semibold px-1 py-0 h-4 text-[10px]">DRIVER</Badge>}
                      <span className="text-muted-foreground">Rides: <span className="font-mono text-foreground">{user.totalRidesTaken || 0}</span></span>
                      <span className="text-muted-foreground">Trips: <span className="font-mono text-foreground">{user.totalRidesDriven || 0}</span></span>
                    </div>
                  </TableCell>
                  <TableCell className="text-right text-sm text-muted-foreground whitespace-nowrap">
                    {user.createdAt ? format(new Date(user.createdAt), 'MMM d, yyyy') : 'Unknown'}
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