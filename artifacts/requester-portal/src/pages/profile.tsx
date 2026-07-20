import * as React from "react";
import { useGetMe, useUpdateProfile, useApplyAsDriver } from "@workspace/api-client-react";
import { User, Phone, Mail, Loader2, ShieldCheck, Car } from "lucide-react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { useQueryClient } from "@tanstack/react-query";
import { getGetMeQueryKey } from "@workspace/api-client-react";
import { format } from "date-fns";

const profileSchema = z.object({
  full_name: z.string().min(2, "Full name is required"),
  phone: z.string().min(5, "Phone number is required"),
});

const driverSchema = z.object({
  license_number: z.string().min(3, "License number is required"),
  vehicle_plate: z.string().min(2, "Plate number is required"),
  vehicle_model: z.string().min(2, "Vehicle model is required"),
});

export default function Profile() {
  const queryClient = useQueryClient();
  const { data: user, isLoading } = useGetMe();
  const updateMut = useUpdateProfile();
  const applyMut = useApplyAsDriver();

  const { register: regProfile, handleSubmit: submitProfile, formState: { errors: errProfile }, reset: resetProfile } = useForm({
    resolver: zodResolver(profileSchema)
  });

  const { register: regDriver, handleSubmit: submitDriver, formState: { errors: errDriver }, reset: resetDriver } = useForm({
    resolver: zodResolver(driverSchema)
  });

  React.useEffect(() => {
    if (user) {
      resetProfile({
        full_name: user.fullName,
        phone: user.phone || ""
      });
      if (user.licenseNumber) {
        resetDriver({
          license_number: user.licenseNumber,
          vehicle_plate: user.vehiclePlate || "",
          vehicle_model: "" // Usually we'd get this from API if available
        });
      }
    }
  }, [user, resetProfile, resetDriver]);

  if (isLoading || !user) {
    return (
      <div className="flex-1 p-8 flex items-center justify-center min-h-[50vh]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  const onProfileSave = (data: any) => {
    updateMut.mutate({ data }, {
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: getGetMeQueryKey() });
        alert("Profile updated successfully");
      }
    });
  };

  const onDriverApply = (data: any) => {
    applyMut.mutate({ data }, {
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: getGetMeQueryKey() });
        alert("Driver application submitted!");
      }
    });
  };

  return (
    <div className="p-6 md:p-8 max-w-4xl mx-auto space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-foreground">Profile Settings</h1>
        <p className="text-muted-foreground mt-1">Manage your account and application status.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        <div className="md:col-span-2 space-y-8">
          <div className="bg-card border border-card-border rounded-xl p-6 shadow-sm">
            <h2 className="text-lg font-semibold flex items-center gap-2 mb-6">
              <User className="h-5 w-5 text-primary" /> Personal Information
            </h2>
            <form onSubmit={submitProfile(onProfileSave)} className="space-y-4">
              <div>
                <label className="block text-sm font-medium mb-1">Full Name</label>
                <input
                  {...regProfile("full_name")}
                  className="w-full px-4 py-2 bg-background border border-input rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
                />
                {errProfile.full_name && <p className="text-destructive text-sm mt-1">{errProfile.full_name.message as string}</p>}
              </div>

              <div>
                <label className="block text-sm font-medium mb-1 text-muted-foreground">Email (Cannot be changed)</label>
                <div className="w-full px-4 py-2 bg-secondary border border-input rounded-lg text-muted-foreground cursor-not-allowed">
                  {user.email}
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium mb-1">Phone Number</label>
                <input
                  {...regProfile("phone")}
                  className="w-full px-4 py-2 bg-background border border-input rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
                />
                {errProfile.phone && <p className="text-destructive text-sm mt-1">{errProfile.phone.message as string}</p>}
              </div>

              <div className="pt-4 flex justify-end">
                <button
                  type="submit"
                  disabled={updateMut.isPending}
                  className="px-6 py-2 bg-primary text-primary-foreground font-medium rounded-lg hover:bg-primary/90 transition-colors flex items-center gap-2"
                >
                  {updateMut.isPending && <Loader2 className="h-4 w-4 animate-spin" />} Save Changes
                </button>
              </div>
            </form>
          </div>

          <div className="bg-card border border-card-border rounded-xl p-6 shadow-sm">
            <div className="flex items-start justify-between mb-6">
              <h2 className="text-lg font-semibold flex items-center gap-2">
                <Car className="h-5 w-5 text-primary" /> Driver / Carrier Application
              </h2>
              {user.isApprovedDriver ? (
                <span className="bg-green-100 text-green-800 text-xs font-semibold px-2.5 py-0.5 rounded-full flex items-center gap-1">
                  <ShieldCheck className="h-3 w-3" /> Approved
                </span>
              ) : (
                <span className="bg-amber-100 text-amber-800 text-xs font-semibold px-2.5 py-0.5 rounded-full">Not Approved</span>
              )}
            </div>

            {user.isApprovedDriver ? (
              <div className="bg-secondary/50 p-4 rounded-lg text-sm text-muted-foreground">
                Your driver account is active. You can pick up parcels and accept rides.
              </div>
            ) : (
              <form onSubmit={submitDriver(onDriverApply)} className="space-y-4">
                <p className="text-sm text-muted-foreground mb-4">Want to earn money by delivering parcels or giving rides? Apply below.</p>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium mb-1">Driver License #</label>
                    <input
                      {...regDriver("license_number")}
                      className="w-full px-4 py-2 bg-background border border-input rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
                    />
                    {errDriver.license_number && <p className="text-destructive text-sm mt-1">{errDriver.license_number.message as string}</p>}
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-1">License Plate</label>
                    <input
                      {...regDriver("vehicle_plate")}
                      className="w-full px-4 py-2 bg-background border border-input rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
                    />
                  </div>
                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium mb-1">Vehicle Model</label>
                    <input
                      {...regDriver("vehicle_model")}
                      className="w-full px-4 py-2 bg-background border border-input rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
                      placeholder="e.g., Toyota Camry 2020"
                    />
                  </div>
                </div>

                <div className="pt-4 flex justify-end">
                  <button
                    type="submit"
                    disabled={applyMut.isPending}
                    className="px-6 py-2 bg-primary text-primary-foreground font-medium rounded-lg hover:bg-primary/90 transition-colors flex items-center gap-2"
                  >
                    {applyMut.isPending && <Loader2 className="h-4 w-4 animate-spin" />} Submit Application
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>

        <div className="space-y-6">
          <div className="bg-card border border-card-border rounded-xl p-6 shadow-sm text-center">
            <div className="h-20 w-20 bg-primary/10 text-primary rounded-full flex items-center justify-center mx-auto mb-4 text-3xl font-bold">
              {user.fullName.charAt(0)}
            </div>
            <h3 className="font-bold text-lg">{user.fullName}</h3>
            <p className="text-muted-foreground text-sm flex items-center justify-center gap-1 mt-1">
              <Mail className="h-3 w-3" /> {user.email}
            </p>
            {user.phone && (
              <p className="text-muted-foreground text-sm flex items-center justify-center gap-1 mt-1">
                <Phone className="h-3 w-3" /> {user.phone}
              </p>
            )}
          </div>

          <div className="bg-card border border-card-border rounded-xl p-6 shadow-sm">
            <h3 className="font-semibold mb-4">Account Stats</h3>
            <div className="space-y-4">
              <div className="flex justify-between items-center border-b border-card-border pb-3">
                <span className="text-sm text-muted-foreground">Role</span>
                <span className="font-medium text-sm capitalize">{user.role.toLowerCase()}</span>
              </div>
              <div className="flex justify-between items-center border-b border-card-border pb-3">
                <span className="text-sm text-muted-foreground">Rating</span>
                <span className="font-medium text-sm flex items-center gap-1">⭐ {user.rating || "New"}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-sm text-muted-foreground">Joined</span>
                <span className="font-medium text-sm">
                  {user.createdAt ? format(new Date(user.createdAt), "MMM yyyy") : "-"}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
