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
package net.runelite.client.plugins.chatbox;

import com.google.inject.Provides;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.inject.Inject;
import javax.swing.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;

import net.runelite.api.events.*;
import net.runelite.client.Notifier;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "A Plugin Tutorial",
	description = "description.",
	tags = {"newplugin", "nomoreahk"},
	type = PluginType.UTILITY
)
@Slf4j
public class ChatboxPlugin extends Plugin {

	@Inject
	private Client client;

	@Inject
	private ChatboxConfig config;

	@Provides
	ChatboxConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(ChatboxConfig.class);
	}

	private JFrame frame;
	private JTextArea chatArea;
	private JTextField chatField;

	@Override
	protected void startUp() {
		externalChatboxSetup();
	}

	@Override
	protected void shutDown() {
		frame.dispose();
	}

	@Subscribe
	private void onChatMessage(ChatMessage event)
	{
		if (event.getType() == ChatMessageType.GAMEMESSAGE) {
			chatArea.setForeground(Color.RED);
			chatArea.append(event.getName() + ": " + event.getMessage() + "\n");
		}
		if (event.getType() == ChatMessageType.PUBLICCHAT) {
			chatArea.setForeground(Color.WHITE);
			chatArea.append(event.getName() + ": " + event.getMessage() + "\n");
		}
		if (event.getType() == ChatMessageType.PRIVATECHAT) {
			chatArea.setForeground(Color.CYAN);
			chatArea.append(event.getName() + ": " + event.getMessage() + "\n");
		}
		if (event.getType() == ChatMessageType.TRADE) {
			chatArea.setForeground(Color.MAGENTA);
			chatArea.append(event.getName() + ": " + event.getMessage() + "\n");
		}

				/*
		String msg = Text.removeTags(event.getMessage()); //remove color
		if (msg.contains("The effects of overload have worn off, and you feel normal again."))
		{
			if (config.overloadNotification())
			{
				notifier.notify("Your overload has worn off");
			}
		}
		else if (msg.contains("A power-up has spawned:"))
		{
			if (msg.contains("Power surge"))
			{
				if (config.powerSurgeNotification())
				{
					notifier.notify(msg);
				}
			}
			else if (msg.contains("Recurrent damage"))
			{
				if (config.recurrentDamageNotification())
				{
					notifier.notify(msg);
				}
			}
			else if (msg.contains("Zapper"))
			{
				if (config.zapperNotification())
				{
					notifier.notify(msg);
				}
			}
			else if (msg.contains("Ultimate force"))
			{
				if (config.ultimateForceNotification())
				{
					notifier.notify(msg);
				}
			}
		}
		*/
	}

	private void externalChatboxSetup() {
		frame = new JFrame();
		chatArea = new JTextArea();
		chatField = new JTextField();

		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setResizable(true);
		frame.setLayout(null);
		frame.setSize(600, 600);
		frame.setTitle("OldSchool RuneScape External Chatbox");

		frame.add(chatArea);
		frame.add(chatField);

		// text Area
		chatArea.setSize(500, 400);
		chatArea.setLocation(2,2);

		// text Field
		chatField.setSize(540, 30);
		chatField.setLocation(2,500);

		chatField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String string = chatField.getText();
				chatArea.append(("YOU: " + string) + "\n");
				chatField.setText("");
			}
		});
	}
}
