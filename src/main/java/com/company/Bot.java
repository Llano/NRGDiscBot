package com.company;

import com.neovisionaries.ws.client.WebSocketFactory;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.exceptions.VerificationLevelException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.io.*;
import java.util.List;

public class Bot extends ListenerAdapter implements Runnable
{
    private String token;
    public JDA jda = null;
    private TextChannel rainChannel;
    private PrivateChannel DMChannel;
    private boolean widthDraw = false;
    public boolean isVerified = true;
    public boolean isReady = false;
    public Bot(String token) {

        this.token = token;
        try {
            jda = new JDABuilder(AccountType.CLIENT).setWebsocketFactory(new WebSocketFactory().setVerifyHostname(false)).setToken(this.token).build();
            jda.addEventListener(this);

        } catch (LoginException e) {
            System.out.println(this.token + " - " +e.getMessage());
            Mailer.sendMail("Login error", this.token + " - " + e.getMessage());
        }

    }

    public String getUserName() {
        return this.jda.getSelfUser().getName();
    }
    public void run() {

    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {


        rainChannel =  this.jda.getTextChannelById(Main.config.getLong("rainchannel.id"));



        if(rainChannel == null) {
            System.out.println(this.getUserName() + " - Rainchannel not found");
            Mailer.sendMail("Error bot onready", this.getUserName() + " - NRG Rain-channel not found. Bot is maybe banned.");

            return;
        }
        for (PrivateChannel p : this.jda.getPrivateChannels()) {
            if (p.getUser().getId().equals("579401311397871617")) {
                DMChannel = p;
            }
        }
        
        if(DMChannel == null) {
            Mailer.sendMail("Error bot onready", "NRG DM channel not found.");

            return;
        }
        isReady = true;

        System.out.println(jda.getSelfUser().getName() + " is ready.");

    }

    public boolean hasAccessToNRGChannel() {

        if(this.jda == null)
            return false;


        return this.jda.getTextChannelById(Main.config.getLong("rainchannel.id")) != null;

    }




    public void shutDown() {
        this.jda.removeEventListener(this);
        this.jda.shutdown();


    }




    @Override
    public void onPrivateMessageReceived(@Nonnull PrivateMessageReceivedEvent event) {



        if (event.getMessage().getAuthor().equals(this.jda.getSelfUser().getId())) {
            return;
        }


        if (event.getAuthor().isBot() && !event.getMessage().getAuthor().getId().equals(579401311397871617L)) {
            System.out.println(this.getUserName() + " - DM from BOT");
            Mailer.sendMail("Message from bot", event.getMessage().getContentDisplay());

        }

        if(event.getAuthor().isBot() && event.getChannel().getId().equals(this.DMChannel.getId()) && event.getMessage().getContentRaw().contains("balance is")) {
            if (this.widthDraw) {
                this.widthDraw = false;
                try {
                    withdrawNRG(event.getMessage().getContentRaw());

                }catch(Exception e) {
                    System.out.println(e.getMessage());
                }
            } else
            {
                System.out.println(this.jda.getSelfUser().getName() + ": " + event.getMessage().getContentStripped());
            }
        }
    }

    public void sendBullshitMessage() {

        MessageHistory history = this.rainChannel.getHistoryBefore(this.rainChannel.getLatestMessageId(), 100).complete();
        for (Message m : history.getRetrievedHistory()) {

            if(m.getContentRaw().matches("^[a-zA-Z\\s]*$") && m.getContentRaw().length() > 15 && m.getContentRaw().length() < 80
            && messageHasRole(m.getMember().getRoles(), "619000857068961792") && !m.getContentRaw().contains("thank"))
            {

                String rephrasedMessage = Rephraser.Rephrase(m.getContentRaw());
                this.rainChannel.sendMessage(rephrasedMessage).complete();
                System.out.println(Main.getCurrentTimeStamp() + " - " + this.getUserName() + ": " + rephrasedMessage + " (" +  (m.getContentRaw()) + ")");
                isVerified = true;



                break;


            }
        }

    }


    public void requestBalance() {
        this.DMChannel.sendMessage("//bal").complete();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        //System.out.println(event.getMessage().getContentRaw());
        /*Message m = event.getMessage();
        if(m.getContentRaw().matches("^[a-zA-Z\\s]*$") && m.getContentRaw().length() > 15 && m.getContentRaw().length() < 80
                && m.getMember().getRoles().size() == 0)
        {

            try {
                new File("sentences.txt").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }


            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream("sentences.txt", true), "utf-8"))) {
                writer.write(m.getContentRaw() + "." +  System.lineSeparator());

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/


    }

    public synchronized void requestWithdrawNRG() {
        this.widthDraw = true;
        this.requestBalance();
    }

    private synchronized void withdrawNRG(String str) {
        double val = Double.parseDouble(str.split("`")[1]);
        val = val - 0.001;

        if (val < 0.001) {
            System.out.println(this.getUserName() + " - Too small amount to withdraw");
            return;
        }
        this.DMChannel.sendMessage("//withdraw " + Main.config.getString("address.receive") + " " + val).queue();
    }

    private boolean messageHasRole(List<Role> roles, String roleid) {
        for (Role role : roles) {
            if (role.getId().equals(roleid)) {
                return true;
            }
        }

        return false;
    }




}