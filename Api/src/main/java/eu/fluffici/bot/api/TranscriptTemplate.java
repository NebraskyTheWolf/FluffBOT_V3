/*
---------------------------------------------------------------------------------
File Name : TranscriptTemplate

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 15/06/2024
Last Modified : 15/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api;

public class TranscriptTemplate {
    public static final String TRANSCRIPT = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Ticket Transcript • %owner%</title>
                <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
                        
                <meta property="og:image" content="https://autumn.fluffici.eu/attachments/eI0QemKZhF6W9EYnDl5JcBGYGvPiIxjPzvrDY_9Klk" />
                <meta property="og:image:secure_url" content="https://autumn.fluffici.eu/attachments/eI0QemKZhF6W9EYnDl5JcBGYGvPiIxjPzvrDY_9Klk" />
                <meta property="og:image:type" content="image/png" />
                <meta property="og:image:width" content="128" />
                        
                <meta name="og:title" content="%owner% • Owner"/>
                <meta name="og:type" content="summary"/>
                        
                <meta name="og:description" content="%ticketid%"/>
                        
                <meta name="apple-mobile-web-app-capable" content="yes">
                <meta content="yes" name="apple-touch-fullscreen" />
                <meta name="apple-mobile-web-app-status-bar-style" content="red">
                <meta name="format-detection" content="telephone=no">
                <meta name="theme-color" content="#FF002E">
                        
              <style>
                @import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css');
                        
                body {
                  font-family: 'Roboto', sans-serif;
                  margin: 0;
                  padding: 20px;
                  background-color: #222;
                  color: #eee;
                  display: flex;
                  justify-content: center;
                  align-items: center;
                  height: 100vh;
                }
                        
                .container {
                  width: 80%;
                }
                        
                .ticket-info, .user-section, .message-box {
                  background-color: #333;
                  padding: 10px;
                  margin-bottom: 20px;
                  border-radius: 10px;
                  overflow: auto;
                }
                        
                .message-box {
                  max-height: 900px;
                }
                        
                .message-header {
                  display: flex;
                  align-items: center;
                  margin-bottom: 10px;
                }
                        
                .avatar {
                  width: 30px;
                  height: 30px;
                  border-radius: 50%;
                  margin-right: 10px;
                }
                        
                .ticket-info p, .user-section p, .message-box p {
                  margin: 5px 0;
                }
                        
                .ticket-owner-label {
                  font-weight: bold;
                  margin-bottom: 5px;
                }
                        
                .ticket-info h2, .user-section h2 {
                  display: flex;
                  align-items: center;
                  color: #ddd;
                }
                        
                .ticket-info h2 i, .user-section h2 i {
                  margin-right: 10px;
                }
                        
                .message-content {
                  background-color: #444;
                  border-radius: 5px;
                  padding: 10px;
                  color: #eee;
                }
                        
                .message-content p {
                  margin: 5px 0;
                  line-height: 1.5;
                }
                        
                .message-content p:last-child {
                  margin-bottom: 0;
                }
                        
                .message-separator {
                  border: none;
                  border-top: 1px solid #666;
                  margin: 20px 0;
                }
                        
                /* Media Queries for Responsive Design */
                @media only screen and (max-width: 600px) {
                  .container {
                    padding: 5px;
                  }
                        
                  .avatar {
                    width: 15px;
                    height: 15px;
                  }
                        
                  .ticket-info h2 i, .user-section h2 i {
                    margin-right: 3px;
                  }
                        
                  .message-content p {
                    margin: 2px 0;
                  }
                        
                  .message-separator {
                    margin: 5px 0;
                  }
                        
                  .card {
                    background-color: #36393f; /* Discord's default card background color */
                    border-radius: 8px; /* Discord's default border radius for cards */
                    padding: 16px; /* Discord's default padding for cards */
                    box-shadow: 0 1px 3px rgba(0,0,0,0.1); /* Discord's default box shadow for cards */
                  }
                        
                  .card-content {
                    color: #ffffff; /* Default text color for Discord */
                  }
                        
                  strong.discord-mention {
                    color: #7289da; /* Discord's default mention color */
                    font-weight: bold; /* Discord's default font weight for mentions */
                    opacity: 0.5; /* 50% opacity */
                  }
                  
                  .print-button {
                       background-color: #FF002E;
                       color: #fff;
                       border: none;
                       padding: 12px 24px;
                       border-radius: 8px;
                       cursor: pointer;
                       font-size: 18px;
                       display: block;
                       margin: 0 auto 20px;
                       text-align: center;
                       transition: background-color 0.3s ease, transform 0.3s ease;
                       box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                   }
                   
                   .print-button:hover {
                       background-color: #e60029;
                       transform: translateY(-2px);
                   }
                   
                   .print-button:active {
                       background-color: #cc0026;
                       transform: translateY(0);
                   }
                }
              </style>
              <script>
                function printPage() {
                     window.print();
                }
              </script>
            </head>
            <body>
            <div class="container">
                <button class="print-button" onclick="printPage()">Print Transcript</button>
                <section class="ticket-info">
                    <h2><i class="fas fa-ticket-alt"></i> Ticket Information</h2>
                    <p><span class="ticket-owner-label"><i class="fas fa-user"></i> Ticket Owner:</span> %owner%</p>
                    <p><i class="fas fa-id-badge"></i> <strong>Ticket ID:</strong> %ticketid%</p>
                    <p><i class="far fa-calendar"></i> <strong>Date of Opening:</strong> %opened%</p>
                    <p><i class="far fa-calendar"></i> <strong>Date of Closing:</strong> %closed%</p>
                    <p><i class="fas fa-hashtag"></i> <strong>Channel ID:</strong> #%channel%</p>
                </section>
                        
                <section class="user-section">
                    <h2><i class="fas fa-users"></i> Users Concerned</h2>
                        
                    %users%
                </section>
                        
                <section class="message-box">
                    %messages%
                </section>
            </div>
            </body>
            </html>
                        
            """;
}