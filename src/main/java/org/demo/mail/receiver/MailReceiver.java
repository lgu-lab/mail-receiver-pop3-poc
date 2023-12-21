package org.demo.mail.receiver;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.demo.mail.receiver.builder.MailMessageBuilder;

import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Session;
import jakarta.mail.Store;

public class MailReceiver {

	public static final boolean DELETE_MSG = true ;
	public static final boolean DO_NOT_DELETE_MSG = false ;

	private static final String PROTOCOL = "pop3" ;
	
	private final Properties properties ;
	private final MailMessageProcessor processor ;
	private final MailMessageBuilder mailMessageBuilder ;
	private final String attachementsDirectory;
	
	public MailReceiver(Properties properties, String attachementsDirectory, MailMessageProcessor processor) {
		this.properties = properties ;
		this.processor = processor;
		this.attachementsDirectory = attachementsDirectory;
		this.mailMessageBuilder = new MailMessageBuilder();
	}
	
	/**
	 * Reads messages for the given user account ( use default mail folder : "INBOX" )
	 * @param mailAccount
	 * @param deleteReadMessages
	 * @return
	 * @throws MailReceiverException
	 */
	public List<MailMessage> readMessages(MailAccount mailAccount, boolean deleteReadMessages) throws MailReceiverException {
		return readMessages(mailAccount, "INBOX", deleteReadMessages);
	}
	
	/**
	 * Reads messages for the given user and the given mail folder
	 * @param mailAccount
	 * @param folderName
	 * @param deleteReadMessages
	 * @return
	 * @throws MailReceiverException
	 */
	public List<MailMessage> readMessages(MailAccount mailAccount, String folderName, boolean deleteReadMessages) throws MailReceiverException {
		// Store implements AutoCloseable
		try (Store store = connectToStore(mailAccount.getUserName(), mailAccount.getUserPassword()) ) {
			// opens the inbox folder
			Folder folder = getFolder(store, folderName);
			return readAndProcessMessages(folder, deleteReadMessages);
		} catch ( MessagingException e ) {
			throw new MailReceiverException("Cannot read messages (MessagingException)", e);
		}
	}

	private Store connectToStore(String userName, String password) throws MailReceiverException {
		Session session = Session.getDefaultInstance(properties);
		try {
			// connects to the message store
			Store store = session.getStore(PROTOCOL);
			store.connect(userName, password);
			return store;
		} catch (NoSuchProviderException e) {
			throw new MailReceiverException("Cannot get Store (NoSuchProviderException)", e);
		} catch (MessagingException e) {
			throw new MailReceiverException("Cannot connect to Store", e);
		}
	}
	
	private Folder getFolder(Store store, String folderName) throws MailReceiverException {
		try {
			return store.getFolder(folderName);
		} catch (Exception e) {
			throw new MailReceiverException("Cannot get folder '" + folderName + "' from store", e);
		}
	}
	
	private List<MailMessage> readAndProcessMessages(Folder folder, boolean deleteReadMessages ) throws MessagingException, MailReceiverException {
		List<MailMessage> messagesReceived = new LinkedList<>();
		// Open server folder with mode READ_ONLY or READ_WRITE for messages deletion 
		folder.open(deleteReadMessages ? Folder.READ_WRITE : Folder.READ_ONLY);
		// fetches new messages from server
		Message[] messages = folder.getMessages();
		// process each message
		for (Message message : messages) {
			// Build "MailMessage" instance from raw Java mail message 
			MailMessage messageReceived = mailMessageBuilder.buildMailMessage(message, attachementsDirectory);
			
			// Delegate message processing to external service
			boolean messageProcessingIsOK = false;
			try {
				messageProcessingIsOK = processor.processMessage(messageReceived);
			} catch (Exception e) {
				throw new MailReceiverException("Message processing error", e);
			}
			messageReceived.setProcessingOK(messageProcessingIsOK);
			
			// Register message
			messagesReceived.add(messageReceived); 
			// Mark the message for future deletion if in "delete" mode
			if ( deleteReadMessages && messageProcessingIsOK ) {
				message.setFlag(Flags.Flag.DELETED, true); // Folder must be in READ_WRITE mode
			}
		}
		// Close server folder with "expunge" flag = true if deletion required 
		// param 'expunge' : expunges all deleted messages if this flag is true
		// in class POP3Folder (extends Folder) : for each message marked "DELETED" => protocol simpleCommand("DELE " + msg);
		// if (expunge && mode == READ_WRITE && !forceClose) 
		//    if (message.isSet(Flags.Flag.DELETED))
		//       port.dele(i + 1);  // POP3 protocol "DELE x"
		folder.close(deleteReadMessages); 
		return messagesReceived;
	}
}
