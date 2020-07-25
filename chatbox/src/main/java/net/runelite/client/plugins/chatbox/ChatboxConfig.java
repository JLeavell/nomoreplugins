/*
 * Copyright (c) 2018, Tomas Slusny <slusnucky@gmail.com>
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

import net.runelite.client.config.*;

import java.awt.Color;


@ConfigGroup("chat")
public interface ChatboxConfig extends Config
{
	@ConfigTitleSection(
			keyName = "firstTitle",
			name = "First Title",
			description = "",
			position = 1
	)
	default Title firstTitle()
	{
		return new Title();
	}

	@ConfigItem(
			position = 3,
			keyName = "secondTitle",
			name = "Hidden if value2",
			description = "This will be hidden if Enum1 is Value1.",
			titleSection = "firstTitle",
			hidden = true,
			unhide = "enum1",
			unhideValue = "VALUE1"

	)
	default String stringConfig() { return "hello"; }

	@ConfigTitleSection(
			position = 4,
			keyName = "secondTitle",
			name = "Second Title",
			description = "This is the second title."

	)
	default Title thirdTitle() { return new Title(); }

	@ConfigItem(
			position = 5,
			keyName = "intConfig",
			name = "int config",
			description = "Set an int value.",
			titleSection = "secondTitle"
	)
	default int intConfig() { return 0; }

	@Range (
			min = 10,
			max = 100
	)
	@ConfigItem(
			position = 5,
			keyName = "intConfigRange",
			name = "int config range",
			description = "Set an int value.",
			titleSection = "secondTitle"
	)
	default int intConfigRange() { return 73; }

	@ConfigItem(
			position = 6,
			keyName = "booleanConfig",
			name = "boolean Config",
			description = "Set a boolean value.",
			titleSection = "secondTitle"
	)
	default boolean booleanConfig() { return false; }

	@ConfigItem(
			position = 7,
			keyName = "colorConfig",
			name = "color Config",
			description = "Set a color value.",
			titleSection = "secondTitle"
	)
	default Color colorConfig() { return Color.WHITE; }







}