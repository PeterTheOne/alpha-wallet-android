package io.stormbird.wallet.di;

import dagger.Module;
import dagger.Provides;
import io.stormbird.wallet.interact.*;
import io.stormbird.wallet.repository.*;
import io.stormbird.wallet.service.AssetDefinitionService;
import io.stormbird.wallet.service.FeeMasterService;
import io.stormbird.wallet.service.GasService;
import io.stormbird.wallet.viewmodel.ImportTokenViewModelFactory;

/**
 * Created by James on 9/03/2018.
 */

@Module
public class ImportTokenModule {

    @Provides
    ImportTokenViewModelFactory importTokenViewModelFactory(
            FindDefaultWalletInteract findDefaultWalletInteract,
            CreateTransactionInteract createTransactionInteract,
            FetchTokensInteract fetchTokensInteract,
            SetupTokensInteract setupTokensInteract,
            FeeMasterService feeMasterService,
            AddTokenInteract addTokenInteract,
            EthereumNetworkRepositoryType ethereumNetworkRepository,
            AssetDefinitionService assetDefinitionService,
            FetchTransactionsInteract fetchTransactionsInteract,
            GasService gasService) {
        return new ImportTokenViewModelFactory(
                findDefaultWalletInteract, createTransactionInteract, fetchTokensInteract, setupTokensInteract, feeMasterService, addTokenInteract, ethereumNetworkRepository, assetDefinitionService, fetchTransactionsInteract, gasService);
    }

    @Provides
    FindDefaultWalletInteract provideFindDefaultWalletInteract(WalletRepositoryType walletRepository) {
        return new FindDefaultWalletInteract(walletRepository);
    }

    @Provides
    CreateTransactionInteract provideCreateTransactionInteract(TransactionRepositoryType transactionRepository, PasswordStore passwordStore) {
        return new CreateTransactionInteract(transactionRepository, passwordStore);
    }

    @Provides
    FetchTokensInteract provideFetchTokensInteract(TokenRepositoryType tokenRepository) {
        return new FetchTokensInteract(tokenRepository);
    }

    @Provides
    SetupTokensInteract provideSetupTokensInteract(TokenRepositoryType tokenRepository) {
        return new SetupTokensInteract(tokenRepository);
    }

    @Provides
    AddTokenInteract provideAddTokenInteract(
            TokenRepositoryType tokenRepository) {
        return new AddTokenInteract(tokenRepository);
    }

    @Provides
    FetchTransactionsInteract provideFetchTransactionsInteract(TransactionRepositoryType transactionRepository, TokenRepositoryType tokenRepositoryType) {
        return new FetchTransactionsInteract(transactionRepository, tokenRepositoryType);
    }
}
