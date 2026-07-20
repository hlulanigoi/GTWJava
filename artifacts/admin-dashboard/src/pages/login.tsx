import React from 'react';
import { useLocation } from 'wouter';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useLogin } from '@workspace/api-client-react';
import { setAuthToken } from '@/lib/auth';
import { Map, AlertCircle } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Form, FormControl, FormField, FormItem, FormMessage } from '@/components/ui/form';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';

const loginSchema = z.object({
  email: z.string().email('Please enter a valid email address'),
  password: z.string().min(1, 'Password is required'),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export default function Login() {
  const [, setLocation] = useLocation();
  const loginMutation = useLogin();

  const form = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '' }
  });

  const onSubmit = (data: LoginFormValues) => {
    loginMutation.mutate(
      { data },
      {
        onSuccess: (result) => {
          setAuthToken(result.token);
          setLocation('/');
        }
      }
    );
  };

  return (
    <div className="min-h-screen flex w-full bg-background">
      <div className="flex-1 flex flex-col justify-center px-4 sm:px-6 lg:flex-none lg:px-20 xl:px-24">
        <div className="mx-auto w-full max-w-sm lg:w-[360px]">
          <div className="mb-8">
            <div className="flex items-center gap-2 font-bold text-2xl tracking-tight text-foreground mb-6">
              <div className="w-8 h-8 bg-primary rounded-md flex items-center justify-center text-primary-foreground">
                <Map className="w-5 h-5" />
              </div>
              <span>Going That Way</span>
            </div>
            <h2 className="text-3xl font-bold tracking-tight text-foreground">Admin Portal</h2>
            <p className="mt-2 text-sm text-muted-foreground">
              Sign in to manage the logistics platform.
            </p>
          </div>

          <div className="mt-8">
            {loginMutation.isError && (
              <Alert variant="destructive" className="mb-6">
                <AlertCircle className="h-4 w-4" />
                <AlertTitle>Authentication Failed</AlertTitle>
                <AlertDescription>
                  {loginMutation.error?.message || 'Invalid email or password. Please try again.'}
                </AlertDescription>
              </Alert>
            )}

            <Form {...form}>
              <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
                <FormField
                  control={form.control}
                  name="email"
                  render={({ field }) => (
                    <FormItem>
                      <Label htmlFor="email">Email address</Label>
                      <FormControl>
                        <Input 
                          id="email" 
                          type="email" 
                          placeholder="admin@goingthatway.com" 
                          {...field} 
                          className="font-mono text-sm"
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="password"
                  render={({ field }) => (
                    <FormItem>
                      <Label htmlFor="password">Password</Label>
                      <FormControl>
                        <Input 
                          id="password" 
                          type="password" 
                          placeholder="••••••••" 
                          {...field} 
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <Button 
                  type="submit" 
                  className="w-full" 
                  size="lg"
                  disabled={loginMutation.isPending}
                >
                  {loginMutation.isPending ? 'Signing in...' : 'Sign in'}
                </Button>
              </form>
            </Form>
          </div>
        </div>
      </div>
      
      {/* Decorative right side */}
      <div className="hidden lg:block relative w-0 flex-1 bg-sidebar border-l border-border/10 overflow-hidden">
        <div className="absolute inset-0 bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjAiIGhlaWdodD0iNjAiIHZpZXdib3g9IjAgMCA2MCA2MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48ZyBmaWxsPSJub25lIiBmaWxsLXJ1bGU9ImV2ZW5vZGQiPjxwYXRoIGQ9Ik0zNiAzNHYtNGgtMnY0aC00djJoNGg0djJoMnYtNGgtNHptMC0zMHY0aC0ydjRoNHYyaDR2LThoLTZ6bTEyIDE0djRoMnY0aDR2LThoLTh6bTEyIDE0aC00djRoNHYtNHptLTEyIDIydjRoMnY0aDR2LThoLTh6bS0xMiAwaC00djRoNHYtNHptLTEyIDB2NGgydjRoNHYtOGgtOHptLTEyIDE0aC00djRoNHYtNHptMTItMTRoLTR2NGg0di00em0tMTIgMHY0aDJ2NGg0di04aC04em0tMTItMTRoNHY0aC00di00em0tMTIgMHY0aDJ2NGg0di04aC04em0xMi0xNGgtNHY0aDR2LTR6bS0xMiAwaDR2NGgtNHYtNHptMTIgMTRoNHY0aC00di00em0xMiAwdi00aDJ2LTRoNHY4aC02em0xMiAwdjRoMnY0aDR2LThoLTh6IiBmaWxsPSIjZmZmZmZmIiBmaWxsLW9wYWNpdHk9IjAuMDUiLz48L2c+PC9zdmc+')] opacity-20"></div>
        <div className="absolute inset-0 bg-gradient-to-tr from-primary/10 to-transparent"></div>
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 flex flex-col items-center justify-center text-center">
          <div className="w-24 h-24 bg-primary/20 rounded-2xl flex items-center justify-center mb-8 backdrop-blur-sm border border-primary/30">
            <Map className="w-12 h-12 text-primary" />
          </div>
          <h2 className="text-4xl font-bold text-white mb-4">Command Center</h2>
          <p className="text-sidebar-foreground/70 max-w-md text-lg">
            Monitor trips, manage users, and track platform revenue in real-time.
          </p>
        </div>
      </div>
    </div>
  );
}