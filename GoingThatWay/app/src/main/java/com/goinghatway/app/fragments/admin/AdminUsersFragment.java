package com.goinghatway.app.fragments.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.goinghatway.app.adapters.admin.AdminUserAdapter;
import com.goinghatway.app.databinding.FragmentAdminUsersBinding;
import com.goinghatway.app.models.User;
import com.goinghatway.app.viewmodels.admin.AdminViewModel;

import java.util.List;

public class AdminUsersFragment extends Fragment implements AdminUserAdapter.Listener {

    private FragmentAdminUsersBinding binding;
    private AdminViewModel viewModel;
    private AdminUserAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminUsersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        adapter = new AdminUserAdapter(this);
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvUsers.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> loadUsers("", null));

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int af) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                loadUsers(s.toString(), null);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadUsers("", null);
    }

    private void loadUsers(String search, String role) {
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.loadUsers(1, search, role).observe(getViewLifecycleOwner(), response -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefresh.setRefreshing(false);
            if (response != null && response.isSuccess() && response.getData() != null) {
                List<User> users = response.getData().getData();
                adapter.setUsers(users);
                binding.tvEmpty.setVisibility(users == null || users.isEmpty()
                        ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onToggleActive(User user) {
        viewModel.setUserActive(user.getId(), !user.isActive())
                .observe(getViewLifecycleOwner(), response -> {
                    if (response != null && response.isSuccess()) {
                        Toast.makeText(requireContext(),
                                user.isActive() ? "User deactivated" : "User activated",
                                Toast.LENGTH_SHORT).show();
                        loadUsers("", null);
                    } else {
                        Toast.makeText(requireContext(), "Action failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
