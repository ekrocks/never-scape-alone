/*
 * Copyright (c) 2022, LlemonDuck <napkinorton@gmail.com>
 * Copyright (c) 2022, Ferrariic <ferrariictweet@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.neverscapealone;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.lang.reflect.Array;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import javax.inject.Inject;

import com.neverscapealone.enums.PingData;
import com.neverscapealone.http.NeverScapeAloneWebsocket;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.grounditems.config.DespawnTimerMode;
import static net.runelite.client.plugins.grounditems.config.ItemHighlightMode.MENU;
import static net.runelite.client.plugins.grounditems.config.ItemHighlightMode.NONE;
import net.runelite.client.plugins.grounditems.config.PriceDisplayMode;
import net.runelite.client.plugins.party.data.PartyTilePingData;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.components.BackgroundComponent;
import net.runelite.client.ui.overlay.components.ProgressPieComponent;
import net.runelite.client.ui.overlay.components.TextComponent;
import net.runelite.client.util.QuantityFormatter;

public class NeverScapeAloneOverlay extends Overlay
{
    private final Client client;
    private final NeverScapeAlonePlugin plugin;
    private final NeverScapeAloneConfig config;
    private final NeverScapeAloneWebsocket websocket;

    @Inject
    private EventBus eventBus;

    @Inject
    private NeverScapeAloneOverlay(Client client, NeverScapeAlonePlugin plugin, NeverScapeAloneConfig config, NeverScapeAloneWebsocket websocket)
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.websocket = websocket;
    }


    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!NeverScapeAloneWebsocket.isSocketConnected){
            NeverScapeAlonePlugin.pingDataArrayList = new ArrayList<>();
            // if the socket isn't connected, don't draw anything
            return null;
        }
        if (Objects.equals(websocket.getGroupID(), "0")){
            // if we're only connected to main lobby, don't draw anything
            return null;
        }
        ArrayList<PingData> pingDataArrayList = NeverScapeAlonePlugin.pingDataArrayList;
        if (pingDataArrayList == null || pingDataArrayList.size() == 0){
            return null;
        }

        for (PingData pingData : pingDataArrayList){
            renderPing(graphics, pingData);
        }

        return null;
    }

    private void renderPing(final Graphics2D graphics, PingData pingData)
    {
        final LocalPoint localPoint = LocalPoint.fromWorld(client, pingData.getX(), pingData.getY());

        if (localPoint == null)
        {
            return;
        }

        final Polygon poly = Perspective.getCanvasTilePoly(client, localPoint);

        if (poly == null)
        {
            return;
        }

        Color color = new Color(pingData.getColorR(), pingData.getColorG(), pingData.getColorB(), pingData.getColorAlpha());
        OverlayUtil.renderPolygon(graphics, poly, color);
    }
}
