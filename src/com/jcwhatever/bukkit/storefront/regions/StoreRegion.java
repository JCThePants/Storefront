package com.jcwhatever.bukkit.storefront.regions;

import com.jcwhatever.bukkit.storefront.Msg;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.nucleus.mixins.IDisposable;
import com.jcwhatever.nucleus.regions.BasicRegion;
import com.jcwhatever.nucleus.regions.IRegion;
import com.jcwhatever.nucleus.regions.IRegionEventHandler;
import com.jcwhatever.nucleus.regions.ReadOnlyRegion;
import com.jcwhatever.nucleus.regions.Region.EnterRegionReason;
import com.jcwhatever.nucleus.regions.Region.LeaveRegionReason;
import com.jcwhatever.nucleus.regions.selection.IRegionSelection;
import com.jcwhatever.nucleus.utils.MetaKey;
import com.jcwhatever.nucleus.utils.PreCon;

import org.bukkit.entity.Player;

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
     * @param store   The owning {@link IStore}.
     * @param region  The external region.
     */
    public StoreRegion(IStore store, IRegion region) {
        _store = store;
        _messageHandler = new MessageHandler();

        setRegion(region);
    }

    /**
     * Constructor.
     *
     * <p>Use to construct with own region.</p>
     *
     * @param store  The owning {@link IStore}.
     */
    public StoreRegion(IStore store) {
        PreCon.notNull(store);

        _store = store;
        _messageHandler = new MessageHandler();

        setOwnRegion();
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
     * Set the stores region.
     *
     * @param region  The region.
     */
    public void setRegion(IRegion region) {
        PreCon.notNull(region);

        if (_region != null)
            dispose();

        _hasOwnRegion = false;

        _region = region;
        _region.addEventHandler(_messageHandler);
        _region.setMeta(REGION_STORE, _store);
    }

    /**
     * Set the store to have its own region.
     */
    public void setOwnRegion() {

        if (_hasOwnRegion)
            return;

        if (_region != null)
            dispose();

        _hasOwnRegion = true;

        BasicRegion region = new BasicRegion(Storefront.getPlugin(), _store.getName(),
                _store.getDataNode().getNode("region"));

        region.setMeta(REGION, region);

        _region = new ReadOnlyRegion(region);
        _region.addEventHandler(_messageHandler);
        _region.setMeta(REGION_STORE, _store);
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

        BasicRegion region = _region.getMeta(REGION);
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
        _region.setMeta(REGION_STORE, null);

        BasicRegion internalRegion = _region.getMeta(REGION);
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
