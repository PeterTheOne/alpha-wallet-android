package io.stormbird.wallet.di;

import android.content.Context;

import com.google.gson.Gson;

import io.stormbird.wallet.interact.SetupTokensInteract;
import io.stormbird.wallet.repository.EthereumNetworkRepository;
import io.stormbird.wallet.repository.EthereumNetworkRepositoryType;
import io.stormbird.wallet.repository.PasswordStore;
import io.stormbird.wallet.repository.PreferenceRepositoryType;
import io.stormbird.wallet.repository.SharedPreferenceRepository;
import io.stormbird.wallet.repository.TokenLocalSource;
import io.stormbird.wallet.repository.TokenRepository;
import io.stormbird.wallet.repository.TokenRepositoryType;
import io.stormbird.wallet.repository.TokensRealmSource;
import io.stormbird.wallet.repository.TransactionLocalSource;
import io.stormbird.wallet.repository.TransactionRepository;
import io.stormbird.wallet.repository.TransactionRepositoryType;
import io.stormbird.wallet.repository.TransactionsRealmCache;
import io.stormbird.wallet.repository.WalletDataRealmSource;
import io.stormbird.wallet.repository.WalletRepository;
import io.stormbird.wallet.repository.WalletRepositoryType;
import io.stormbird.wallet.service.*;

import java.io.File;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module
public class RepositoriesModule {
	@Singleton
	@Provides
	PreferenceRepositoryType providePreferenceRepository(Context context) {
		return new SharedPreferenceRepository(context);
	}

	@Singleton
	@Provides
	AccountKeystoreService provideAccountKeyStoreService(Context context) {
        File file = new File(context.getFilesDir(), "keystore/keystore");
		return new GethKeystoreAccountService(file);
	}

	@Singleton
    @Provides
    TickerService provideTickerService(OkHttpClient httpClient, Gson gson) {
	    //return new TrustWalletTickerService(httpClient, gson);
		return new CoinmarketcapTickerService(httpClient, gson);
    }

	@Singleton
	@Provides
	EthereumNetworkRepositoryType provideEthereumNetworkRepository(
            PreferenceRepositoryType preferenceRepository,
            TickerService tickerService) {
		return new EthereumNetworkRepository(preferenceRepository, tickerService);
	}

	@Singleton
	@Provides
    WalletRepositoryType provideWalletRepository(
			PreferenceRepositoryType preferenceRepositoryType,
			AccountKeystoreService accountKeystoreService,
			EthereumNetworkRepositoryType networkRepository,
			TransactionsNetworkClientType blockExplorerClient,
			WalletDataRealmSource walletDataRealmSource,
			OkHttpClient httpClient) {
		return new WalletRepository(
		        preferenceRepositoryType, accountKeystoreService, networkRepository, blockExplorerClient, walletDataRealmSource, httpClient);
	}

	@Singleton
	@Provides
	TransactionRepositoryType provideTransactionRepository(
			EthereumNetworkRepositoryType networkRepository,
			AccountKeystoreService accountKeystoreService,
			TransactionsNetworkClientType blockExplorerClient,
            TransactionLocalSource inDiskCache) {
		return new TransactionRepository(
				networkRepository,
				accountKeystoreService,
				inDiskCache,
				blockExplorerClient);
	}

	@Singleton
    @Provides
    TransactionLocalSource provideTransactionInDiskCache(RealmManager realmManager) {
        return new TransactionsRealmCache(realmManager);
    }

	@Singleton
	@Provides
    TransactionsNetworkClientType provideBlockExplorerClient(
			OkHttpClient httpClient,
			Gson gson,
			EthereumNetworkRepositoryType ethereumNetworkRepository,
			Context context) {
		return new TransactionsNetworkClient(httpClient, gson, ethereumNetworkRepository, context);
	}

	@Singleton
    @Provides
    TokenRepositoryType provideTokenRepository(
            EthereumNetworkRepositoryType ethereumNetworkRepository,
            TokenLocalSource tokenLocalSource,
            TickerService tickerService,
			GasService gasService) {
	    return new TokenRepository(
	            ethereumNetworkRepository,
                tokenLocalSource,
                tickerService,
				gasService);
    }

	@Singleton
    @Provides
    TokenExplorerClientType provideTokenService(OkHttpClient okHttpClient, Gson gson) {
	    return new EthplorerTokenService(okHttpClient, gson);
    }

    @Singleton
    @Provides
    TokenLocalSource provideRealmTokenSource(RealmManager realmManager, EthereumNetworkRepositoryType ethereumNetworkRepository) {
	    return new TokensRealmSource(realmManager, ethereumNetworkRepository);
    }

	@Singleton
	@Provides
	WalletDataRealmSource provideRealmWalletDataSource(RealmManager realmManager) {
		return new WalletDataRealmSource(realmManager);
	}

	@Singleton
	@Provides
	TokensService provideTokensService(EthereumNetworkRepositoryType ethereumNetworkRepository, RealmManager realmManager, OkHttpClient okHttpClient) {
		return new TokensService(ethereumNetworkRepository, realmManager, okHttpClient);
	}

	@Singleton
	@Provides
	GasService provideGasService(EthereumNetworkRepositoryType ethereumNetworkRepository) {
		return new GasService(ethereumNetworkRepository);
	}

	@Singleton
	@Provides
	MarketQueueService provideMarketQueueService(Context ctx, OkHttpClient okHttpClient,
												 TransactionRepositoryType transactionRepository,
												 PasswordStore passwordStore) {
		return new MarketQueueService(ctx, okHttpClient, transactionRepository, passwordStore);
	}

	@Singleton
	@Provides
	OpenseaService provideOpenseaService(Context ctx) {
		return new OpenseaService(ctx);
	}

	@Singleton
	@Provides
	FeeMasterService provideFeemasterService(OkHttpClient okHttpClient,
											 TransactionRepositoryType transactionRepository,
											 PasswordStore passwordStore) {
		return new FeeMasterService(okHttpClient, transactionRepository, passwordStore);
	}

	@Singleton
	@Provides
	ImportTokenService provideImportTokenService(OkHttpClient okHttpClient,
												 TransactionRepositoryType transactionRepository,
												 PasswordStore passwordStore) {
		return new ImportTokenService(okHttpClient, transactionRepository, passwordStore);
	}

	@Singleton
	@Provides
	NotificationService provideNotificationService(Context ctx) {
		return new NotificationService(ctx);
	}

	@Singleton
	@Provides
	AssetDefinitionService provideAssetDefinitionService(OkHttpClient okHttpClient, Context ctx, NotificationService notificationService, RealmManager realmManager, EthereumNetworkRepositoryType ethereumNetworkRepository, TokensService tokensService, TokenLocalSource tls) {
		return new AssetDefinitionService(okHttpClient, ctx, notificationService, realmManager, ethereumNetworkRepository, tokensService, tls);
	}
}
