/*
 * Copyright (c) 2018, James Swindle <wilingua@gmail.com>
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
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
package net.runelite.client.plugins.newplugin;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCDefinition;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.util.Text;
import net.runelite.client.graphics.ModelOutlineRenderer;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class NpcSceneOverlay extends Overlay
{
	private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

	// Anything but white text is quite hard to see since it is drawn on
	// a dark background
	private static final Color TEXT_COLOR = Color.WHITE;

	private final Client client;
	private final NpcIndicatorsPlugin plugin;
	private final NpcIndicatorsConfig config;
	private final ModelOutlineRenderer modelOutliner;

	@Inject
	NpcSceneOverlay(final Client client, final NpcIndicatorsPlugin plugin, final NpcIndicatorsConfig config, final ModelOutlineRenderer modelOutliner)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.modelOutliner = modelOutliner;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.showRespawnTimer())
		{
			plugin.getDeadNpcsToDisplay().forEach((id, npc) -> renderNpcRespawn(npc, graphics));
		}

		for (NPC npc : plugin.getHighlightedNpcs())
		{
			renderNpcOverlay(graphics, npc, config.getHighlightColor());
		}

		return null;
	}

	private void renderNpcRespawn(final MemorizedNpc npc, final Graphics2D graphics)
	{
		if (npc.getPossibleRespawnLocations().isEmpty())
		{
			return;
		}

		final WorldPoint respawnLocation = npc.getPossibleRespawnLocations().get(0);
		final LocalPoint lp = LocalPoint.fromWorld(client, respawnLocation.getX(), respawnLocation.getY());

		if (lp == null)
		{
			return;
		}

		final Color color = config.getHighlightColor();

		final LocalPoint centerLp = new LocalPoint(
				lp.getX() + Perspective.LOCAL_TILE_SIZE * (npc.getNpcSize() - 1) / 2,
				lp.getY() + Perspective.LOCAL_TILE_SIZE * (npc.getNpcSize() - 1) / 2);

		final Polygon poly = Perspective.getCanvasTileAreaPoly(client, centerLp, npc.getNpcSize());

		if (poly != null)
		{
			OverlayUtil.renderPolygon(graphics, poly, color);
		}


		final String timeLeftStr = plugin.formatTime(plugin.getTimeLeftForNpc(npc));

		final int textWidth = graphics.getFontMetrics().stringWidth(timeLeftStr);
		final int textHeight = graphics.getFontMetrics().getAscent();

		final Point canvasPoint = Perspective
				.localToCanvas(client, centerLp, respawnLocation.getPlane());

		if (canvasPoint != null)
		{
			final Point canvasCenterPoint = new Point(
					canvasPoint.getX() - textWidth / 2,
					canvasPoint.getY() + textHeight / 2);

			OverlayUtil.renderTextLocation(graphics, canvasCenterPoint, timeLeftStr, TEXT_COLOR);
		}
	}

	private void renderNpcOverlay(Graphics2D graphics, NPC actor, Color color)
	{
		NPCDefinition npcDefinition = actor.getTransformedDefinition();
		if (npcDefinition == null || !npcDefinition.isInteractible()
				|| (actor.isDead() && config.ignoreDeadNpcs()))
		{
			return;
		}

		if (config.drawInteracting() && actor.getInteracting() != null
				&& actor.getInteracting() == client.getLocalPlayer())
		{
			color = config.getInteractingColor();
		}

		switch (config.renderStyle())
		{
			case SOUTH_WEST_TILE:
			{
				int size = 1;
				NPCDefinition composition = actor.getTransformedDefinition();
				if (composition != null)
				{
					size = composition.getSize();
				}

				LocalPoint localPoint = actor.getLocalLocation();

				int x = localPoint.getX() - ((size - 1) * Perspective.LOCAL_TILE_SIZE / 2);
				int y = localPoint.getY() - ((size - 1) * Perspective.LOCAL_TILE_SIZE / 2);

				Polygon tilePoly = Perspective.getCanvasTilePoly(client, new LocalPoint(x, y));

				renderPoly(graphics, color, tilePoly);
				break;
			}
			case TILE:
			{
				int size = 1;
				NPCDefinition composition = actor.getTransformedDefinition();
				if (composition != null)
				{
					size = composition.getSize();
				}
				final LocalPoint lp = actor.getLocalLocation();
				final Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size);
				renderPoly(graphics, color, tilePoly);
				break;
			}
			case THIN_TILE:
			{
				int size = 1;
				NPCDefinition composition = actor.getTransformedDefinition();
				if (composition != null)
				{
					size = composition.getSize();
				}
				final LocalPoint lp = actor.getLocalLocation();
				final Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size);
				renderPoly(graphics, color, tilePoly, 1);
				break;
			}
			case HULL:
				final Shape objectClickbox = actor.getConvexHull();
				graphics.setColor(color);
				graphics.draw(objectClickbox);
				break;
			case THIN_OUTLINE:
				modelOutliner.drawOutline(actor, 1, color);
				break;
			case OUTLINE:
				modelOutliner.drawOutline(actor, 2, color);
				break;
			case THIN_GLOW:
				modelOutliner.drawOutline(actor, 4, color, TRANSPARENT);
				break;
			case GLOW:
				modelOutliner.drawOutline(actor, 8, color, TRANSPARENT);
				break;
			case TRUE_LOCATIONS:
			{
				int size = 1;
				NPCDefinition composition = actor.getTransformedDefinition();

				if (composition != null)
				{
					size = composition.getSize();
				}

				final WorldPoint wp = actor.getWorldLocation();
				final Color squareColor = color;

				getSquare(wp, size).forEach(square ->
						drawTile(graphics, square, squareColor, 1, 255, 50));
				break;
			}
			case FILL:
				Shape fill = actor.getConvexHull();
				if (fill != null) {

					if (config.isInteractingIndicator()) {
						if (client.getLocalPlayer().getInteracting() == actor) {
							graphics.setColor(config.interactingIndicatorColour());
							graphics.fillRect(config.interactingIndicatorX(), config.interactingIndicatorY(), config.interactingIndicatorWidth(), config.interactingIndicatorHeight());
						}
					}

					if (config.showMouseHover() && actor.getConvexHull().contains(client.getMouseCanvasPosition().getX(), client.getMouseCanvasPosition().getY())) {
						graphics.setColor(config.mouseHoverIndicatorColour());
						graphics.fillRect(config.mouseHoverX(), config.mouseHoverY(), config.mouseHoverIndicatorWidth(), config.mouseHoverIndicatorHeight());
					}

					if (config.hideNPCInCombat() && !actor.getName().toLowerCase().contains("banker")) {
						if (actor.getHealthRatio() == 0 || actor.getInteracting() != null) {
							break;
						}
					}

					if (actor.getInteracting() == client.getLocalPlayer()) {
						OverlayUtil.renderFilledPolygon(graphics, actor.getConvexHull(), config.getInteractingColor());
					}
					else {
						OverlayUtil.renderFilledPolygon(graphics, actor.getConvexHull(), color);
					}
				}
				break;
			case BOX:
				Shape box = actor.getConvexHull();
				if (box != null) {
					if (config.isInteractingIndicator()) {
						if (client.getLocalPlayer().getInteracting() == actor) {
							graphics.setColor(config.interactingIndicatorColour());
							graphics.fillRect(config.interactingIndicatorX(), config.interactingIndicatorY(), config.interactingIndicatorWidth(), config.interactingIndicatorHeight());
						}
					}

					if (config.showMouseHover() && actor.getConvexHull().contains(client.getMouseCanvasPosition().getX(), client.getMouseCanvasPosition().getY())) {
						graphics.setColor(config.mouseHoverIndicatorColour());
						graphics.fillRect(config.mouseHoverX(), config.mouseHoverY(), config.mouseHoverIndicatorWidth(), config.mouseHoverIndicatorHeight());
					}

					if (config.hideNPCInCombat() && !actor.getName().toLowerCase().contains("banker")) {
						if (actor.getHealthRatio() == 0 || actor.getInteracting() != null) {
							break;
						}
					}

					if (actor.getInteracting() == client.getLocalPlayer()) {
						int x = (int) box.getBounds().getCenterX() - config.boxSize() / 2;
						int y = (int) box.getBounds().getCenterY() - config.boxSize() / 2;
						graphics.setColor(config.getInteractingColor());
						graphics.fillRect(x, y, config.boxSize(), config.boxSize());
					}
					else {
						int x = (int) box.getBounds().getCenterX() - config.boxSize() / 2;
						int y = (int) box.getBounds().getCenterY() - config.boxSize() / 2;
						graphics.setColor(color);
						graphics.fillRect(x, y, config.boxSize(), config.boxSize());
					}
				}
				break;
		}

		if (config.drawNames() && actor.getName() != null)
		{
			final String npcName = Text.removeTags(actor.getName());
			final Point textLocation = actor.getCanvasTextLocation(graphics, npcName, actor.getLogicalHeight() + 40);

			if (textLocation != null)
			{
				OverlayUtil.renderTextLocation(graphics, textLocation, npcName, color);
			}
		}

		if (config.drawInteracting() && actor.getInteracting() != null)
		{
			final int drawHeight = config.drawNames() ? 80 : 40;
			final String targetName = Text.removeTags(actor.getInteracting().getName());
			final Point textLocation = actor.getCanvasTextLocation(graphics, targetName, actor.getLogicalHeight() + drawHeight);

			if (textLocation != null)
			{
				OverlayUtil.renderTextLocation(graphics, textLocation, targetName, color);
			}
		}
	}

	private void renderPoly(Graphics2D graphics, Color color, Polygon polygon)
	{
		if (polygon != null)
		{
			graphics.setColor(color);
			graphics.setStroke(new BasicStroke(2));
			graphics.draw(polygon);
			graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
			graphics.fill(polygon);
		}
	}

	private void renderPoly(Graphics2D graphics, Color color, Polygon polygon, int width)
	{
		if (polygon != null)
		{
			graphics.setColor(color);
			graphics.setStroke(new BasicStroke(width));
			graphics.draw(polygon);
			graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
			graphics.fill(polygon);
		}
	}

	private List<WorldPoint> getSquare(WorldPoint npcLoc, int npcSize)
	{
		return new WorldArea(npcLoc.getX(), npcLoc.getY(), npcSize, npcSize, npcLoc.getPlane()).toWorldPointList();
	}

	private void drawTile(Graphics2D graphics, WorldPoint point, Color color, int strokeWidth, int outlineAlpha, int fillAlpha)
	{
		WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();

		if (point.distanceTo(playerLocation) >= 32)
		{
			return;
		}

		LocalPoint lp = LocalPoint.fromWorld(client, point);

		if (lp == null)
		{
			return;
		}

		Polygon poly = Perspective.getCanvasTilePoly(client, lp);

		if (poly == null)
		{
			return;
		}

		graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), outlineAlpha));
		graphics.setStroke(new BasicStroke(strokeWidth));
		graphics.draw(poly);
		graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), fillAlpha));
		graphics.fill(poly);
	}
}