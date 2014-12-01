package com.jcwhatever.bukkit.storefront.regions;

import com.jcwhatever.bukkit.generic.mixins.IDisposable;
import com.jcwhatever.bukkit.generic.regions.BasicRegion;
import com.jcwhatever.bukkit.generic.regions.IRegion;
import com.jcwhatever.bukkit.generic.regions.IRegionEventHandler;
import com.jcwhatever.bukkit.generic.regions.ReadOnlyRegion;
import com.jcwhatever.bukkit.generic.regions.Region.EnterRegionReason;
import com.jcwhatever.bukkit.generic.regions.Region.LeaveRegionReason;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.storefront.Msg;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.stores.IStore;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

/*
 * 
 */
public class StoreRegion implements IDisposable{

    private final IStore _store;
    private final MessageHandler _messageHandler;

    private IRegion _region;
    private String _entryMessage;
    private String _exitMessage;
    private boolean _hasOwnRegion;
    private boolean _isDisposed;


    /**
     * Constructor
     */
    public StoreRegion(IStore store, ReadOnlyRegion region) {
        _store = store;
        _messageHandler = new MessageHandler();

        setRegion(region);
    }

    public StoreRegion(IStore store) {
        PreCon.notNull(store);

        _store = store;
        _messageHandler = new MessageHandler();

        setOwnRegion();
    }

    public boolean hasOwnRegion() {
        return _hasOwnRegion;
    }

    public IRegion getRegion() {
        return _region;
    }

    public void setRegion(IRegion region) {
        PreCon.notNull(region);

        if (_region != null) {
            dispose();
        }

        _hasOwnRegion = false;

        _region = region;
        _region.addEventHandler(_messageHandler);
        _region.setMeta(IStore.class.getName(), _store);
    }

    public void setOwnRegion() {

        if (_region != null) {
            dispose();
        }

        _hasOwnRegion = true;

        BasicRegion region = new BasicRegion(Storefront.getInstance(), _store.getName(),
                _store.getDataNode().getNode("region"));

        region.setMeta(BasicRegion.class.getName() + ".Storefront", region);

        _region = new ReadOnlyRegion(region);
        _region.addEventHandler(_messageHandler);
        _region.setMeta(IStore.class.getName(), _store);
    }

    public void setCoords(Location p1, Location p2) {
        if (!hasOwnRegion())
            setOwnRegion();

        BasicRegion region = _region.getMeta(BasicRegion.class.getName() + ".Storefront");
        if (region == null)
            throw new AssertionError();

        region.setCoords(p1, p2);
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
        _region.setMeta(IStore.class.getName(), null);

        BasicRegion internalRegion = _region.getMeta(BasicRegion.class.getName() + ".Storefront");
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
