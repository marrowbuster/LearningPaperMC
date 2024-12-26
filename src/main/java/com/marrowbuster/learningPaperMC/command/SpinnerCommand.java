package com.marrowbuster.learningPaperMC.command;

import com.marrowbuster.learningPaperMC.LearningPaperMC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static com.marrowbuster.learningPaperMC.listener.MainListener.MAX_ITEM_COUNT;
import static com.marrowbuster.learningPaperMC.listener.MainListener.MIN_ITEM_COUNT;

public class SpinnerCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players may execute this command.").color(NamedTextColor.RED));
        } else if (args.length < 3) {
            sender.sendMessage(Component.text("Not enough arguments. Command usage: set item-count <integer>")
                                       .color(NamedTextColor.RED));
        } else if (!args[0].equals("set")) {
            sender.sendMessage(Component.text("Unknown subcommand. Available: set").color(NamedTextColor.RED));
        } else if (!args[1].equals("item-count")) {
            sender.sendMessage(Component.text("Unknown subcommand. Available: item-count").color(NamedTextColor.RED));
        } else {
            final int itemCount;

            try {
                itemCount = Integer.parseUnsignedInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(
                        Component.text("Provided item count wasn't a positive integer.").color(NamedTextColor.RED));
                return true;
            }

            if (itemCount < MIN_ITEM_COUNT || itemCount > MAX_ITEM_COUNT) {
                sender.sendMessage(Component.text(
                                "Provided item count must be between %s and %s inclusive.".formatted(MIN_ITEM_COUNT,
                                                                                                     MAX_ITEM_COUNT))
                                           .color(NamedTextColor.RED));
                return true;
            }

            LearningPaperMC.getMainListener().getPlayerData(player.getUniqueId()).setItemCount(itemCount, player);

            sender.sendMessage(Component.text("Item count for spinning swords has been set to ")
                                       .color(NamedTextColor.AQUA)
                                       .append(Component.text(itemCount + "").color(NamedTextColor.DARK_AQUA))
                                       .append(Component.text(".").color(NamedTextColor.AQUA)));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String label, @NotNull String[] args) {
        return switch (args.length) {
            case 1 -> List.of("set");
            case 2 -> List.of("item-count");
            case 3 -> IntStream.range(3, 8).mapToObj(Integer::toString).toList();
            default -> Collections.emptyList();
        };
    }
}