package com.jcwhatever.storefront.regions;

import com.jcwhatever.storefront.Msg;
import com.jcwhatever.storefront.Storefront;
import com.jcwhatever.storefront.stores.IStore;
import com.jcwhatever.nucleus.Nucleus;
import com.jcwhatever.nucleus.mixins.IDisposable;
import com.jcwhatever.nucleus.regions.BasicRegion;
import com.jcwhatever.nucleus.regions.IRegion;
import com.jcwhatever.nucleus.regions.IRegionEventHandler;
import com.jcwhatever.nucleus.regions.ReadOnlyRegion;
import com.jcwhatever.nucleus.regions.options.EnterRegionReason;
import com.jcwhatever.nucleus.regions.options.LeaveRegionReason;
import com.jcwhatever.nucleus.regions.selection.IRegionSelection;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.DependencyRunner;
import com.jcwhatever.nucleus.utils.DependencyRunner.DependencyStatus;
import com.jcwhatever.nucleus.utils.DependencyRunner.IDependantRunnable;
import com.jcwhatever.nucleus.utils.MetaKey;
import com.jcwhatever.nucleus.utils.PreCon;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;

/**
 * Encapsulates a stores region.
 *
 * <p>Used to hold either the stores own region or a region
 * attached from another source (external region).</p>
 */
public class StoreRegion implements IDisposable{

    public static final MetaKey<IStore> REGION_STORE = new MetaKey<>(IStore.class);
    public static final MetaKey<BasicRegion> REGION = new MetaKey<>(BasicRegion.class);

    private final IStore _store;
    private final IDataNode _dataNode;
    private final MessageHandler _messageHandler;

    private IRegion _region;
    private String _entryMessage;
    private String _exitMessage;
    private boolean _hasOwnRegion;
    private boolean _isDisposed;

    /**
     * Constructor.
     *
     * <p>Used to construct with an initial external region.</p>
     *
     * @param store    The owning {@link IStore}.
     * @param dataNode  The regions data node.
     */
    public StoreRegion(IStore store, final IDataNode dataNode) {
        PreCon.notNull(store);
        PreCon.notNull(dataNode);

        _store = store;
        _dataNode = dataNode;
        _messageHandler = new MessageHandler();

        final String pluginName = dataNode.getString("plugin");
        final String regionName = dataNode.getString("region");
        if (pluginName != null && regionName != null) {

            // setup external region
            DependencyRunner<IDependantRunnable> runner = new DependencyRunner<>(Storefront.getPlugin());
            runner.add(new IDependantRunnable() {

                IRegion region;

                @Override
                public DependencyStatus getDependencyStatus() {

                    Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
                    if (plugin == null)
                        return DependencyStatus.NOT_READY;

                    region = Nucleus.getRegionManager().getRegion(plugin, regionName);
                    if (region == null)
                        return DependencyStatus.NOT_READY;

                    return DependencyStatus.READY;
                }

                @Override
                public void run() {
                    _region = region;
                    _region.getMeta().set(REGION_STORE, _store);
                    _region.addEventHandler(_messageHandler);
                }
            });
            runner.start();
        }
        else {
            // setup own region
            BasicRegion region = new BasicRegion(Storefront.getPlugin(), _store.getName(),
                    dataNode);
            region.getMeta()
                    .set(REGION, region)
                    .set(REGION_STORE, _store);
            region.addEventHandler(_messageHandler);
            _region = region;
            _hasOwnRegion = true;
        }
    }

    /**
     * Determine if the store has its own region
     * or is using an external region.
     */
    public boolean hasOwnRegion() {
        return _hasOwnRegion;
    }

    /**
     * Get the stores region.
     */
    public IRegion getRegion() {
        return _region;
    }

    /**
     * Set an external region.
     *
     * @param region  The region.
     */
    public void setRegion(IRegion region) {
        PreCon.notNull(region);

        if (_region != null && _region.getPlugin().equals(Storefront.getPlugin()))
            dispose();

        _hasOwnRegion = false;

        _region = region;
        _region.addEventHandler(_messageHandler);
        _region.getMeta().set(REGION_STORE, _store);

        _dataNode.clear();
        _dataNode.set("plugin", region.getPlugin().getName());
        _dataNode.set("region", region.getName());
        _dataNode.save();
    }

    /**
     * Set the store to have its own region.
     */
    public void setOwnRegion() {

        if (_hasOwnRegion)
            return;

        if (_region != null && _region.getPlugin().equals(Storefront.getPlugin()))
            dispose();

        _hasOwnRegion = true;

        _dataNode.clear();
        _dataNode.save();

        BasicRegion region = new BasicRegion(Storefront.getPlugin(), _store.getName(),
                _dataNode);

        region.getMeta()
                .set(REGION, region)
                .set(REGION_STORE, _store);

        _region = new ReadOnlyRegion(region);
        _region.addEventHandler(_messageHandler);



    }

    /**
     * Set the stores region coordinates.
     *
     * <p>Also sets the store to have its own region.</p>
     *
     * @param selection  The region selection.
     */
    public void setCoords(IRegionSelection selection) {
        if (!hasOwnRegion())
            setOwnRegion();

        BasicRegion region = _region.getMeta().get(REGION);
        if (region == null)
            throw new AssertionError();

        region.setCoords(selection.getP1(), selection.getP2());
    }

    @Nullable
    public String getEntryMessage() {
        return _entryMessage;
    }

    @Nullable
    public String getExitMessage() {
        return _exitMessage;
    }

    public void setEntryMessage(@Nullable String message) {
        _entryMessage = message;
    }

    public void setExitMessage(@Nullable String message) {
        _exitMessage = message;
    }

    @Override
    public boolean isDisposed() {
        return _isDisposed;
    }

    @Override
    public void dispose() {
        _region.removeEventHandler(_messageHandler);
        _region.getMeta().set(REGION_STORE, null);

        BasicRegion internalRegion = _region.getMeta().get(REGION);
        if (internalRegion != null) {
            internalRegion.dispose();
        }

        _isDisposed = true;
    }

    private class MessageHandler implements IRegionEventHandler {

        @Override
        public boolean canDoPlayerEnter(Player player, EnterRegionReason reason) {
            return _entryMessage != null;
        }

        @Override
        public boolean canDoPlayerLeave(Player player, LeaveRegionReason reason) {
            return _exitMessage != null;
        }

        @Override
        public void onPlayerEnter(Player player, EnterRegionReason reason) {
            Msg.tell(player, _entryMessage);
        }

        @Override
        public void onPlayerLeave(Player player, LeaveRegionReason reason) {
            Msg.tell(player, _exitMessage);
        }
    }
}
