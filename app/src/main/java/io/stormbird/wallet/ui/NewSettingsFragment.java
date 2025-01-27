package io.stormbird.wallet.ui;


import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import io.stormbird.wallet.C;
import io.stormbird.wallet.R;
import io.stormbird.wallet.entity.Wallet;
import io.stormbird.wallet.util.LocaleUtils;
import io.stormbird.wallet.viewmodel.NewSettingsViewModel;
import io.stormbird.wallet.viewmodel.NewSettingsViewModelFactory;
import io.stormbird.wallet.widget.AWalletConfirmationDialog;
import io.stormbird.wallet.widget.SelectLocaleDialog;

import static io.stormbird.wallet.C.CHANGED_LOCALE;
import static io.stormbird.wallet.ui.HomeActivity.RC_ASSET_EXTERNAL_WRITE_PERM;

public class NewSettingsFragment extends Fragment {
    @Inject
    NewSettingsViewModelFactory newSettingsViewModelFactory;

    private NewSettingsViewModel viewModel;
    private Wallet wallet;

    private TextView walletsSubtext;
    private TextView localeSubtext;
    private Switch notificationState;
    private LinearLayout layoutEnableXML;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        viewModel = ViewModelProviders.of(this, newSettingsViewModelFactory).get(NewSettingsViewModel.class);
        viewModel.defaultWallet().observe(this, this::onDefaultWallet);
        viewModel.setLocale(getContext());

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        walletsSubtext = view.findViewById(R.id.wallets_subtext);
        localeSubtext = view.findViewById(R.id.locale_lang_subtext);
        notificationState = view.findViewById(R.id.switch_notifications);

        TextView helpText = view.findViewById(R.id.text_version);
        try
        {
            String version = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
            helpText.setText(version);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

        localeSubtext.setText(LocaleUtils.getDisplayLanguage(viewModel.getDefaultLocale(), viewModel.getDefaultLocale()));

        updateNotificationState();

        final LinearLayout layoutWalletAddress = view.findViewById(R.id.layout_wallet_address);
        layoutWalletAddress.setOnClickListener(v -> {
            viewModel.showMyAddress(getContext());
        });

        final LinearLayout layoutManageWallets = view.findViewById(R.id.layout_manage_wallets);
        layoutManageWallets.setOnClickListener(v -> {
            viewModel.showManageWallets(getContext(), false);
        });

        final LinearLayout layoutSwitchnetworks = view.findViewById(R.id.layout_switch_network);
        layoutSwitchnetworks.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SelectNetworkActivity.class);
            intent.putExtra(C.EXTRA_SINGLE_ITEM, false);
            getActivity().startActivity(intent);
        });

        final LinearLayout layoutSwitchLocale = view.findViewById(R.id.layout_locale_lang);
        layoutSwitchLocale.setOnClickListener(v -> {
            String currentLocale = viewModel.getDefaultLocale();
            SelectLocaleDialog dialog = new SelectLocaleDialog(getActivity(), viewModel.getLocaleList(getContext()), currentLocale);
            dialog.setOnClickListener(v1 -> {
                if (!currentLocale.equals(dialog.getSelectedItemId())) {
                    viewModel.setDefaultLocale(getContext(), dialog.getSelectedItemId());
                    localeSubtext.setText(LocaleUtils.getDisplayLanguage(dialog.getSelectedItemId(), currentLocale));
                    getActivity().sendBroadcast(new Intent(CHANGED_LOCALE));
                }
                dialog.dismiss();
            });
            dialog.show();
        });

        final LinearLayout layoutHelp = view.findViewById(R.id.layout_help_faq);
        layoutHelp.setOnClickListener(v -> {
            viewModel.showHelp(getContext());
        });

        final LinearLayout layoutTelegram = view.findViewById(R.id.layout_telegram);
        layoutTelegram.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(C.AWALLET_TELEGRAM_URL));
            if (isAppAvailable(C.TELEGRAM_PACKAGE_NAME)) {
                intent.setPackage(C.TELEGRAM_PACKAGE_NAME);
            }
            startActivity(intent);
        });

        final LinearLayout layoutTwitter = view.findViewById(R.id.layout_twitter);
        layoutTwitter.setOnClickListener(v -> {
            Intent intent;
            try {
                getActivity().getPackageManager().getPackageInfo(C.TWITTER_PACKAGE_NAME, 0);
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(C.AWALLET_TWITTER_ID));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            } catch (Exception e) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(C.AWALLET_TWITTER_URL));
            }
            startActivity(intent);
        });

        final LinearLayout layoutNotifications = view.findViewById(R.id.layout_notification_settings);
        layoutNotifications.setOnClickListener(v -> {
            boolean currentState = viewModel.getNotificationState();
            currentState = !currentState;
            viewModel.setNotificationState(currentState);
            updateNotificationState();
        });

        LinearLayout layoutFacebook = view.findViewById(R.id.layout_facebook);
        layoutFacebook.setOnClickListener(v -> {
            Intent intent;
            try {
                getActivity().getPackageManager().getPackageInfo(C.FACEBOOK_PACKAGE_NAME, 0);
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(C.AWALLET_FACEBOOK_ID));
            } catch (Exception e) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(C.AWALLET_FACEBOOK_URL));
            }
            startActivity(intent);
        });

        layoutEnableXML = view.findViewById(R.id.layout_xml_override);
        if (checkWritePermission() == false) {
            layoutEnableXML.setVisibility(View.VISIBLE);
            layoutEnableXML.setOnClickListener(v -> {
                //ask OS to ask user if we can use the 'AlphaWallet' directory
                showXMLOverrideDialog();
            });
        }

        return view;
    }

    private boolean isAppAvailable(String packageName) {
        PackageManager pm = getActivity().getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void updateNotificationState() {
        boolean state = viewModel.getNotificationState();
        notificationState.setChecked(state);
    }

    private void onDefaultWallet(Wallet wallet) {
        this.wallet = wallet;
        walletsSubtext.setText(wallet.address);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.prepare();
    }

    private void showXMLOverrideDialog() {
        AWalletConfirmationDialog cDialog = new AWalletConfirmationDialog(getActivity());
        cDialog.setTitle(R.string.enable_xml_override_dir);
        cDialog.setSmallText(R.string.explain_xml_override);
        cDialog.setMediumText(R.string.ask_user_about_xml_override);
        cDialog.setPrimaryButtonText(R.string.dialog_ok);
        cDialog.setPrimaryButtonListener(v -> {
            //ask for OS permission and write directory
            askWritePermission();
            cDialog.dismiss();
        });
        cDialog.setSecondaryButtonText(R.string.dialog_cancel_back);
        cDialog.setSecondaryButtonListener(v -> {
            cDialog.dismiss();
        });
        cDialog.show();
    }

    private boolean checkWritePermission() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void refresh() {
        if (layoutEnableXML != null) {
            if (checkWritePermission()) {
                layoutEnableXML.setVisibility(View.GONE);
            } else {
                layoutEnableXML.setVisibility(View.VISIBLE);
            }
        }
    }

    private void askWritePermission() {
        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        Log.w("SettingsFragment", "Folder write permission is not granted. Requesting permission");
        ActivityCompat.requestPermissions(getActivity(), permissions, RC_ASSET_EXTERNAL_WRITE_PERM);
    }
}
