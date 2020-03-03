package com.company;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.VerificationLevelException;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;



public class Main {

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";


	public static Configuration config;
	private static boolean run = true;
    public static void main(String[] args) {


		Configurations configs = new Configurations();
		try
		{
			config = configs.properties(new File("config.txt"));

		}
		catch (ConfigurationException cex)
		{
			System.out.println(cex.toString());
			return;
		}




        Scanner in = new Scanner(System.in);
		final int[] index = {0};
    	List<Bot> botList = new ArrayList<Bot>();
		List<Object> users = config.getList("user.token");

		for (Object token: users) {
			Bot bot = new Bot(token.toString());



            if (bot.jda == null) {
                continue;
            }

            try {
                bot.jda.awaitStatus(JDA.Status.CONNECTED);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
                continue;
            }

            if(!bot.isReady) continue;

            botList.add(bot);
            Thread t = new Thread(bot);
            t.start();
		}


        if(botList.size() < 1) {
            System.out.println("No bots started");
            return;
        }

        List<Bot> botsRunningNonThreadSafe = new ArrayList<>();
        List<Bot> botsRunning = Collections.synchronizedList(botsRunningNonThreadSafe);

        new Thread(() -> {
            while(botList.size() > 0) {


                Bot bot = botList.get(index[0]);
                try
                {
                    bot.sendBullshitMessage();
                    if(!botsRunning.contains(bot)) botsRunning.add(bot);



                } catch(Exception e) {

                    if (e instanceof VerificationLevelException) {
                        System.out.println(Main.getCurrentTimeStamp() + " - " + bot.getUserName() + " verification needed");
                        if (bot.isVerified) {
                            Mailer.sendMail("Verification needed", "For user " + bot.getUserName());
                            bot.isVerified = false;

                        }
                        if(botsRunning.contains(bot)) botsRunning.remove(bot);


                    }else if(e instanceof  InsufficientPermissionException) {
                        System.out.println(Main.getCurrentTimeStamp() + " - " + bot.getUserName() + " don't have permission to post right now");
                        if (bot.isReady && bot.hasAccessToNRGChannel()) {
                            Mailer.sendMail("NRG Bot error", bot.getUserName() + " doesn't have permission to post. Bot is maybe banned?");
                            bot.isReady = false;

                        }
                        if(botsRunning.contains(bot)) botsRunning.remove(bot);

                    }else
                    {
                        System.out.println(Main.getCurrentTimeStamp() + " - " + e.getMessage());

                    }

                }
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(config.getInt("users.interval") / botList.size()) + TimeUnit.SECONDS.toMillis(ThreadLocalRandom.current().nextInt(60, (60*4) + 1)));
                } catch (InterruptedException e) {
                    System.out.println(e.toString());
                }

                index[0]++;
                if (index[0] >= botList.size()) {
                    index[0] = 0;

                    if (botsRunning.size() == 0) {
                        System.out.println("No bots running.. Sleeping for 5 minutes");
                        try {
                            Thread.sleep(60000 * 5);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }



            }

        }).start();



		new Thread(() -> {
			while(run) {
				String s = in.nextLine();
				if(s.equals("b")) {
					System.out.println("Requesting balance..");

					for (Bot b : botList) {
						try {
							b.requestBalance();

						}catch(Exception e) {
							System.out.println(e.getMessage());
						}
					}
				}else if(s.equals("w")) {
					for (Bot b : botList) {
						System.out.println("Withdrawing balance for " + b.getUserName());

						b.requestWithdrawNRG();
					}

				}else if(s.equals("x")){
					for (Bot b : botList) {

						b.shutDown();
					}
					run = false;
					//timer.cancel();
				}else if(s.equals("p")) {
					for (Bot b : botList) {
						System.out.println(b.getUserName() + " - " + b.jda.getSelfUser().getPhoneNumber());
					}
				}

			}
		}).start();


        new Thread(() -> {
            while(true) {
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis((60 * 60 * 3)));

                    for(Bot b : botsRunning) {
                        Thread.sleep(ThreadLocalRandom.current().nextInt((60000 * 15), (60000 * 58) + 1));
                        b.requestWithdrawNRG();
                    }
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }

            }
        }).start();


    }

	public static String getCurrentTimeStamp() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}
}
