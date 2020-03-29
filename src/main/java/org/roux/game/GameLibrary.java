package org.roux.game;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.roux.FileManager;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class GameLibrary {

    private final List<Game> library = new ArrayList<>();

    public GameLibrary() {
        // Check if they are games already to put in library
        Map<String, JSONObject> jsonGames = getJsonGames();
        if(jsonGames != null && !jsonGames.isEmpty()) {
            jsonGames.forEach((name, jsonObject) -> {
                String path = jsonObject.get("path").toString();
                String[] keywords = (String[]) ((JSONArray) jsonObject.get("keywords")).toArray(new String[0]);
                Game game = new Game(path, name, keywords);
                library.add(game);
            });
        }
    }

    public List<Game> scan() {
        final Map<String, JSONObject> oldJsonGames = getJsonGames();
        final List<Game> newGames = new ArrayList<>();
        List<Path> files = FileManager.getFilesInFolders();
        filterToKeepGames(files);
        for(Path path : files) {
            Game game = new Game(path);
            JSONObject oldGame;
            if(oldJsonGames != null && (oldGame = oldJsonGames.get(game.getName())) != null) {
                game.getKeywords().addAll(((JSONArray) oldGame.get("keywords")));
            }
            newGames.add(game);
        }
        library.clear();
        library.addAll(newGames);
        return this.library;
    }

    public List<String> filter(final SortedSet<String> entries, String inputText) {
        final List<String> filteredEntries;
        filteredEntries = entries.stream()
                .filter(entry -> entry.toLowerCase().startsWith(inputText.toLowerCase()))
                .collect(Collectors.toList());
        filteredEntries.addAll(
                library.stream()
                        .filter(entry -> !filteredEntries.contains(entry.getName()))
                        .filter(game -> game.getKeywords().contains(inputText.toLowerCase()))
                        .map(Game::getName)
                        .collect(Collectors.toList())
        );
        filteredEntries.addAll(
                entries.stream()
                        .filter(entry -> !filteredEntries.contains(entry))
                        .filter(entry -> entry.toLowerCase().contains(inputText.toLowerCase()))
                        .collect(Collectors.toList())
        );
        return filteredEntries;
    }

    private List<Path> filterToKeepGames(List<Path> files) {
        // @todo filtrer en fait lol
        return files;
    }

    public Map<String, JSONObject> getJsonGames() {
        final Map<String, JSONObject> nameToObjectMap = new HashMap<>();
        JSONArray jsonArray = FileManager.getJsonArray("games");
        if(jsonArray != null) {
            Iterator<JSONObject> iterator = jsonArray.iterator();
            iterator.forEachRemaining(jsonObject -> nameToObjectMap.put(jsonObject.get("name").toString(), jsonObject));
            return nameToObjectMap;
        }
        return null;
    }

    public JSONArray getLibraryAsJsonArray() {
        JSONArray gameArray = new JSONArray();
        for(Game game : library) {
            JSONObject jsonGame = new JSONObject();
            jsonGame.put("path", game.getExecutablePath().toString());
            jsonGame.put("name", game.getName());
            JSONArray jsonArray = new JSONArray();
            jsonArray.addAll(game.getKeywords());
            jsonGame.put("keywords", jsonArray);
            gameArray.add(jsonGame);
        }
        return gameArray;
    }

    public boolean addGame(Game game) {
        return library.add(game);
    }

    public Game getGame(String name) {
        return library.stream()
                .filter(game -> game.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public List<Game> getLibrary() {
        return library;
    }
}
