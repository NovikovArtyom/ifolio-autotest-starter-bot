package it.aces.autotests.starter.service;

import it.aces.autotests.starter.config.BotConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    private BotConfig config;
    private final static String BASE_URL = "https://cidev.ifolio.cloud/job/autotest/job/{}/buildWithParameters";

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String message;

            switch (messageText) {
                case "/showjobs":
                    showjobsCommands(chatId);
                    break;
                case "/public_staging@IFOLIO_Autotest_Starter_Bot":
                    message = "Public section tests on staging have been launched!";
                    sendRequest(chatId, "ifolio_staging", message);
                    break;
                case "/public_prod@IFOLIO_Autotest_Starter_Bot":
                    message = "Public section tests on prod have been launched!";
                    sendRequest(chatId, "ifolio_prod", message);
                    break;
                case "/admin_staging@IFOLIO_Autotest_Starter_Bot":
                    message = "Admin section tests on staging have been launched!";
                    sendRequest(chatId, "ifolio_admin", message);
                    break;
                case "/admin_prod@IFOLIO_Autotest_Starter_Bot":
                    message = "Admin section tests on prod have been launched!";
                    sendRequest(chatId, "ifolio_admin_prod", message);
                    break;
                case "/paypage@IFOLIO_Autotest_Starter_Bot":
                    message = "Paypage section tests have been launched!";
                    sendRequest(chatId, "ifolio_paypage", message);
                    break;
                default:
                    sendMessage(chatId, "Sorry, command was not recognized");
            }
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    private void showjobsCommands(long chatId) {
        String startAnswer = "Here is the list of jobs to start:\n/public_staging\n/public_prod\n/admin_staging" +
                "\n/admin_prod\n/paypage";
        sendMessage(chatId, startAnswer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendRequest(long chatId, String path, String message) {
        RestTemplate restTemplate = new RestTemplate();

        String url = BASE_URL.replace("{}", path);
        String user = "anovikov";
        String token = "110a10e33a7e0f69b387a84dd0168a9e09";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(user, token);

        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            sendMessage(chatId, message);
        } catch (HttpClientErrorException e) {
            sendMessage(chatId, "There's been an error! This job may be temporarily disabled.");
        }
    }
}
