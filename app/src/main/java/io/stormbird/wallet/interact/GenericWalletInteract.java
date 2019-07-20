package io.stormbird.wallet.interact;

import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.realm.Realm;
import io.stormbird.wallet.entity.Wallet;
import io.stormbird.wallet.repository.WalletRepositoryType;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.stormbird.wallet.service.HDKeyService;

public class GenericWalletInteract
{
	private final WalletRepositoryType walletRepository;

	public GenericWalletInteract(WalletRepositoryType walletRepository) {
		this.walletRepository = walletRepository;
	}

	public Single<Wallet> find() {
		return walletRepository
				.getDefaultWallet()
				.onErrorResumeNext(walletRepository
						.fetchWallets()
						.to(single -> Flowable.fromArray(single.blockingGet()))
						.firstOrError()
						.flatMapCompletable(walletRepository::setDefaultWallet)
						.andThen(walletRepository.getDefaultWallet()))
				.observeOn(AndroidSchedulers.mainThread());
	}

	/**
	 * Called when wallet marked as backed up.
	 * Update the wallet date
	 *
	 * @param walletAddr
	 */
	public Disposable updateBackupTime(String walletAddr)
	{
		return walletRepository.updateBackupTime(walletAddr);
	}

	/**
	 * Called when wallet has had its backup warning dismissed
	 * Update the wallet date
	 *
	 * @param walletAddr
	 */
	public Disposable updateWarningTime(String walletAddr)
	{
		return walletRepository.updateWarningTime(walletAddr);
	}

	public Single<String> getWalletNeedsBackup()
	{
		return walletRepository.getWalletRequiresBackup();
	}

	/**
	 * Check if a wallet needs to be backed up.
	 * @param walletAddr
	 * @return
	 */
	public Single<BackupLevel> getBackupLevel(String walletAddr)
	{
		return walletRepository.getWalletBackupLevel(walletAddr);
	}

	public enum BackupLevel
	{
		BACKUP_NOT_REQUIRED, PERIODIC_BACKUP, WALLET_NEVER_BACKED_UP
	}
}