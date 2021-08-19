package com.mcool.sai.ui.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aefyr.sai.R;
import com.mcool.sai.adapters.SaiPiSessionsAdapter;
import com.mcool.sai.ui.dialogs.AppInstalledDialogFragment;
import com.mcool.sai.ui.dialogs.DonationSuggestionDialogFragment;
import com.mcool.sai.ui.dialogs.ErrorLogDialogFragment2;
import com.mcool.sai.ui.dialogs.InstallationConfirmationDialogFragment;
import com.mcool.sai.ui.dialogs.InstallerXDialogFragment;
import com.mcool.sai.ui.recycler.RecyclerPaddingDecoration;
import com.mcool.sai.utils.PreferencesHelper;
import com.mcool.sai.utils.Utils;
import com.mcool.sai.utils.saf.SafUtils;
import com.mcool.sai.viewmodels.InstallerViewModel;

import java.util.Collections;
import java.util.Objects;

public class Installer2Fragment extends InstallerFragment implements InstallationConfirmationDialogFragment.ConfirmationListener, SaiPiSessionsAdapter.ActionDelegate {
    private static final String TAG = "Installer2Fragment";

    private InstallerViewModel mViewModel;

    private RecyclerView mSessionsRecycler;
    private ViewGroup mPlaceholderContainer;

    private PreferencesHelper mHelper;

    private Uri mPendingActionViewUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHelper = PreferencesHelper.getInstance(getContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPlaceholderContainer = findViewById(R.id.container_installer_placeholder);

        mSessionsRecycler = findViewById(R.id.rv_installer_sessions);
        mSessionsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        SaiPiSessionsAdapter sessionsAdapter = new SaiPiSessionsAdapter(requireContext());
        sessionsAdapter.setActionsDelegate(this);

        mSessionsRecycler.setAdapter(sessionsAdapter);
        mSessionsRecycler.addItemDecoration(new RecyclerPaddingDecoration(0, requireContext().getResources().getDimensionPixelSize(R.dimen.installer_sessions_recycler_top_padding), 0, requireContext().getResources().getDimensionPixelSize(R.dimen.installer_sessions_recycler_bottom_padding)));

        mViewModel = new ViewModelProvider(this).get(InstallerViewModel.class);
        mViewModel.getEvents().observe(getViewLifecycleOwner(), (event) -> {
            if (event.isConsumed())
                return;

            //For some reason this observer gets called after state save on some devices
            if (isStateSaved())
                return;

            if (event.type().equals(InstallerViewModel.EVENT_PACKAGE_INSTALLED))
                DonationSuggestionDialogFragment.showIfNeeded(requireContext(), getChildFragmentManager());

            if (!mHelper.showInstallerDialogs()) {
                event.consume();
                return;
            }

            switch (event.type()) {
                case InstallerViewModel.EVENT_PACKAGE_INSTALLED:
                    showPackageInstalledAlert(event.consume());
                    break;
                case InstallerViewModel.EVENT_INSTALLATION_FAILED:
                    String[] errors = event.consume();
                    ErrorLogDialogFragment2.newInstance(getString(R.string.installer_installation_failed), errors[0], errors[1], false).show(getChildFragmentManager(), "installation_error_dialog");
                    break;
            }
        });
        mViewModel.getSessions().observe(getViewLifecycleOwner(), (sessions) -> {
            setPlaceholderShown(sessions.size() == 0);
            sessionsAdapter.setData(sessions);
        });

        Button installButtton = findViewById(R.id.button_install);
        installButtton.setOnClickListener((v) -> {
            // 总是使用专业模式
            openInstallerXDialog(null);
        });

        if (mPendingActionViewUri != null) {
            handleActionView(mPendingActionViewUri);
            mPendingActionViewUri = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void handleActionView(Uri uri) {
        if (!isAdded()) {
            mPendingActionViewUri = uri;
            return;
        }

        if (mHelper.isInstallerXEnabled()) {
            openInstallerXDialog(uri);
        } else {
            DialogFragment existingDialog = (DialogFragment) getChildFragmentManager().findFragmentByTag("installation_confirmation_dialog");
            if (existingDialog != null)
                existingDialog.dismiss();

            InstallationConfirmationDialogFragment.newInstance(uri).show(getChildFragmentManager(), "installation_confirmation_dialog");
        }

    }

    private void setPlaceholderShown(boolean shown) {
        if (shown) {
            mPlaceholderContainer.setVisibility(View.VISIBLE);
            mSessionsRecycler.setVisibility(View.GONE);
        } else {
            mPlaceholderContainer.setVisibility(View.GONE);
            mSessionsRecycler.setVisibility(View.VISIBLE);
        }
    }

    private void openInstallerXDialog(@Nullable Uri apkSourceUri) {
        DialogFragment existingDialog = (DialogFragment) getChildFragmentManager().findFragmentByTag("installerx_dialog");
        if (existingDialog != null)
            existingDialog.dismiss();

        InstallerXDialogFragment.newInstance(apkSourceUri, null).show(getChildFragmentManager(), "installerx_dialog");
    }

    private void showPackageInstalledAlert(String packageName) {
        AppInstalledDialogFragment.newInstance(packageName).show(getChildFragmentManager(), "dialog_app_installed");
    }

    @Override
    public void onConfirmed(Uri apksFileUri) {
        String fileName = SafUtils.getFileNameFromContentUri(requireContext(), apksFileUri);
        if (fileName == null) {
            Log.w(TAG, String.format("Unable to get file name from uri %s, assuming it's a .apks file", apksFileUri.toString()));
            mViewModel.installPackagesFromContentProviderZip(apksFileUri);
            return;
        }

        String fileExtension = Utils.getExtension(fileName);
        if (fileExtension == null) {
            Log.w(TAG, String.format("Unable to get extension from uri %s, assuming it's a .apks file", apksFileUri.toString()));
            mViewModel.installPackagesFromContentProviderZip(apksFileUri);
            return;
        }

        switch (fileExtension.toLowerCase()) {
            case "apks":
                mViewModel.installPackagesFromContentProviderZip(apksFileUri);
                break;
            case "apk":
                mViewModel.installPackagesFromContentProviderUris(Collections.singletonList(apksFileUri));
                break;
            default:
                Log.w(TAG, String.format("Uri %s has unexpected extension - %s, assuming it's a .apks file", apksFileUri.toString(), fileExtension));
                mViewModel.installPackagesFromContentProviderZip(apksFileUri);
                break;
        }
    }

    @Override
    protected int layoutId() {
        return R.layout.fragment_installer2;
    }

    @Override
    public void launchApp(String packageName) {
        try {
            PackageManager pm = requireContext().getPackageManager();
            Intent appLaunchIntent = pm.getLaunchIntentForPackage(packageName);
            Objects.requireNonNull(appLaunchIntent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(appLaunchIntent);
        } catch (Exception e) {
            Log.w("SAI", e);
            Toast.makeText(requireContext(), R.string.installer_unable_to_launch_app, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showError(String shortError, String fullError) {
        ErrorLogDialogFragment2.newInstance(getString(R.string.installer_installation_failed), shortError, fullError, false).show(getChildFragmentManager(), "installation_error_dialog");
    }
}
