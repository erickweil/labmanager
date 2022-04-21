/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.erickweil.labproxy.dns;

// Copyright (c) 1999-2004 Brian Wellington (bwelling@xbill.org)

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xbill.DNS.Address;
import org.xbill.DNS.Cache;
import org.xbill.DNS.Credibility;
import org.xbill.DNS.DClass;
import org.xbill.DNS.ExtendedFlags;
import org.xbill.DNS.Flags;
import org.xbill.DNS.Header;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.OPTRecord;
import org.xbill.DNS.Opcode;
import org.xbill.DNS.RRset;
import org.xbill.DNS.Rcode;
//import org.xbill.DNS.ZoneTransferException;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.SetResponse;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TSIG;
import org.xbill.DNS.TSIGRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;
import org.xbill.DNS.Zone;
import org.xbill.DNS.ZoneTransferException;

/** @author Brian Wellington &lt;bwelling@xbill.org&gt; */

public class jnamed {

static final int FLAG_DNSSECOK = 1;
static final int FLAG_SIGONLY = 2;

Map caches;
Map znames;
Map TSIGs;

public String started_time = null;
public String dns_server = "8.8.8.8";
public String email = "erickweil2@gmail.com";
public String dns_blocked = "virtual.ifro.edu.br";
public String interface_indentifier = "PCI";
public String room_name = "CASA";
public Record blocked_record;
public HashMap<String,Record[]> cache_mapping;


public String[] domains_blacklist_regex;

public String[] domains_blacklist_words;
public DomainEntry domains_blacklist;

private synchronized Record[] get_cache(String name)
{
    return cache_mapping.get(name);
}
private synchronized void put_cache(String name,Record[] records)
{
    cache_mapping.put(name,records);
}
private synchronized void regex_blocked_log(String name)
{
    BufferedWriter output = null;
    try {
        File log_folder = new File(System.getenv("appdata")+ "\\dnsproxy\\");
        File logFile = new File(log_folder,"blacklist-regexmatched.txt");
        output = new BufferedWriter(new FileWriter(logFile, true)); //opens file
        char[] name_b = (name+"\n").toCharArray();
        output.write(name_b,0,name_b.length); //writes to file
    } catch (FileNotFoundException ex) {
        Logger.getLogger(jnamed.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
        Logger.getLogger(jnamed.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
        try {
            output.close();
        } catch (IOException ex) {
            Logger.getLogger(jnamed.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
private synchronized void printLog(String msg)
{
    System.out.println(Util.horaBonita()+": "+msg);
}
private static String
addrport(InetAddress addr, int port) {
	return addr.getHostAddress() + "#" + port;
}

public
jnamed(String conffile) throws IOException, ZoneTransferException {
	FileInputStream fs;
	InputStreamReader isr;
	BufferedReader br;
	List ports = new ArrayList();
	List addresses = new ArrayList();
	try {
		fs = new FileInputStream(conffile);
		isr = new InputStreamReader(fs);
		br = new BufferedReader(isr);
	}
	catch (Exception e) {
		System.out.println("Cannot open " + conffile);
		return;
	}
        
        started_time = Util.horaBonita();
        
	try {
                cache_mapping = new HashMap<>();
                
                try
                {
                    domains_blacklist = DomainBlacklistParser.getBlacklist(new File("blacklist.txt"));
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    domains_blacklist = new DomainEntry(false);
                }
                
                try
                {
                    domains_blacklist_words = new String(Files.readAllBytes(new File("blacklist-words.txt").toPath()),Charset.forName("UTF-8")).split("\r?\n");
                    domains_blacklist_regex = new String(Files.readAllBytes(new File("blacklist-regex.txt").toPath()),Charset.forName("UTF-8")).split("\r?\n");
                }
                catch(Exception e)
                {
                    e.printStackTrace(); 
                    domains_blacklist_words = new String[0];
                    domains_blacklist_regex = new String[0];
                }
                /*domains_blacklist =new DomainEntry(false);
                DomainEntry entry;
                DomainEntry entryy;
                
                entry = new DomainEntry(false);
                entry.put("facebook", new DomainEntry(true));
                
                domains_blacklist.put("com", entry);
                
                entry = new DomainEntry(false);
                entry.put("fbcdn", new DomainEntry(true));
                
                domains_blacklist.put("net", entry);
                
                entryy = new DomainEntry(false);
                entryy.put("scratch", new DomainEntry(true));
                
                entry = new DomainEntry(false);
                entry.put("mit", entryy);
                
                domains_blacklist.put("edu", entry);*/
                
		caches = new HashMap();
		znames = new HashMap();
		TSIGs = new HashMap();

		String line = null;
		while ((line = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line);
			if (!st.hasMoreTokens())
				continue;
			String keyword = st.nextToken();
			if (!st.hasMoreTokens()) {
				System.out.println("Invalid line: " + line);
				continue;
			}
			if (keyword.charAt(0) == '#')
				continue;
			if (keyword.equals("primary"))
				addPrimaryZone(st.nextToken(), st.nextToken());
                        else if (keyword.equals("dns"))
                        {
				dns_server = st.nextToken();
                                System.out.println("configured with: "+dns_server);
                        }
                        else if (keyword.equals("email"))
                        {
				email = st.nextToken();
                                System.out.println("sending e-mails to: "+email);
                        }
                        else if (keyword.equals("interface_id"))
                        {
				interface_indentifier = st.nextToken();
                        }
                        else if (keyword.equals("room_name"))
                        {
				room_name = st.nextToken();
                        }
			else if (keyword.equals("secondary"))
				addSecondaryZone(st.nextToken(),
						 st.nextToken());
			else if (keyword.equals("cache")) {
				Cache cache = new Cache(st.nextToken());
				caches.put(new Integer(DClass.IN), cache);
			} else if (keyword.equals("key")) {
				String s1 = st.nextToken();
				String s2 = st.nextToken();
				if (st.hasMoreTokens())
					addTSIG(s1, s2, st.nextToken());
				else
					addTSIG("hmac-md5", s1, s2);
			} else if (keyword.equals("port")) {
				ports.add(Integer.valueOf(st.nextToken()));
			} else if (keyword.equals("address")) {
				String addr = st.nextToken();
				addresses.add(Address.getByAddress(addr));
			} else {
				System.out.println("unknown keyword: " +
						   keyword);
			}

		}

		if (ports.size() == 0)
			ports.add(new Integer(53));

		if (addresses.size() == 0)
			addresses.add(Address.getByAddress("0.0.0.0"));

		Iterator iaddr = addresses.iterator();
		while (iaddr.hasNext()) {
			InetAddress addr = (InetAddress) iaddr.next();
			Iterator iport = ports.iterator();
			while (iport.hasNext()) {
				int port = ((Integer)iport.next()).intValue();
				addUDP(addr, port);
				addTCP(addr, port);
				System.out.println("jnamed: listening on " +
						   addrport(addr, port));
			}
		}
		System.out.println("jnamed: running");
	}
	finally {
		fs.close();
	}
}

public void
addPrimaryZone(String zname, String zonefile) throws IOException {
	Name origin = null;
	if (zname != null)
		origin = Name.fromString(zname, Name.root);
	Zone newzone = new Zone(origin, zonefile);
	znames.put(newzone.getOrigin(), newzone);
}

public void
addSecondaryZone(String zone, String remote)
throws IOException, ZoneTransferException
{
	Name zname = Name.fromString(zone, Name.root);
	Zone newzone = new Zone(zname, DClass.IN, remote);
	znames.put(zname, newzone);
}

public void
addTSIG(String algstr, String namestr, String key) throws IOException {
	Name name = Name.fromString(namestr, Name.root);
	TSIGs.put(name, new TSIG(algstr, namestr, key));
}

public Cache
getCache(int dclass) {
	Cache c = (Cache) caches.get(new Integer(dclass));
	if (c == null) {
		c = new Cache(dclass);
		caches.put(new Integer(dclass), c);
	}
	return c;
}

public Zone
findBestZone(Name name) {
	Zone foundzone = null;
	foundzone = (Zone) znames.get(name);
	if (foundzone != null)
		return foundzone;
	int labels = name.labels();
	for (int i = 1; i < labels; i++) {
		Name tname = new Name(name, i);
		foundzone = (Zone) znames.get(tname);
		if (foundzone != null)
			return foundzone;
	}
	return null;
}

public RRset
findExactMatch(Name name, int type, int dclass, boolean glue) {
	Zone zone = findBestZone(name);
	if (zone != null)
		return zone.findExactMatch(name, type);
	else {
		RRset [] rrsets;
		Cache cache = getCache(dclass);
		if (glue)
			rrsets = cache.findAnyRecords(name, type);
		else
			rrsets = cache.findRecords(name, type);
		if (rrsets == null)
			return null;
		else
			return rrsets[0]; /* not quite right */
	}
}

void
addRRset(Name name, Message response, RRset rrset, int section, int flags) {
	for (int s = 1; s <= section; s++)
		if (response.findRRset(name, rrset.getType(), s))
			return;
	if ((flags & FLAG_SIGONLY) == 0) {
		Iterator it = rrset.rrs();
		while (it.hasNext()) {
			Record r = (Record) it.next();
			if (r.getName().isWild() && !name.isWild())
				r = r.withName(name);
			response.addRecord(r, section);
		}
	}
	if ((flags & (FLAG_SIGONLY | FLAG_DNSSECOK)) != 0) {
		Iterator it = rrset.sigs();
		while (it.hasNext()) {
			Record r = (Record) it.next();
			if (r.getName().isWild() && !name.isWild())
				r = r.withName(name);
			response.addRecord(r, section);
		}
	}
}

private final void
addSOA(Message response, Zone zone) {
	response.addRecord(zone.getSOA(), Section.AUTHORITY);
}

private final void
addNS(Message response, Zone zone, int flags) {
	RRset nsRecords = zone.getNS();
	addRRset(nsRecords.getName(), response, nsRecords,
		 Section.AUTHORITY, flags);
}

private final void
addCacheNS(Message response, Cache cache, Name name) {
	SetResponse sr = cache.lookupRecords(name, Type.NS, Credibility.HINT);
	if (!sr.isDelegation())
		return;
	RRset nsRecords = sr.getNS();
	Iterator it = nsRecords.rrs();
	while (it.hasNext()) {
		Record r = (Record) it.next();
		response.addRecord(r, Section.AUTHORITY);
	}
}

private void
addGlue(Message response, Name name, int flags) {
	RRset a = findExactMatch(name, Type.A, DClass.IN, true);
	if (a == null)
		return;
	addRRset(name, response, a, Section.ADDITIONAL, flags);
}

private void
addAdditional2(Message response, int section, int flags) {
	Record [] records = response.getSectionArray(section);
	for (int i = 0; i < records.length; i++) {
		Record r = records[i];
		Name glueName = r.getAdditionalName();
		if (glueName != null)
			addGlue(response, glueName, flags);
	}
}

private final void
addAdditional(Message response, int flags) {
	addAdditional2(response, Section.ANSWER, flags);
	addAdditional2(response, Section.AUTHORITY, flags);
}

public boolean doDnsRequest(Message response,String nameString) throws UnknownHostException, TextParseException
{
    Name n = Name.fromString(nameString);
    Lookup l = new Lookup(nameString);//, Type.TXT, DClass.CH);
    l.setResolver(new SimpleResolver(dns_server));
    for(int i=0;i<3;i++)
    {
        Record[] records = l.run();
        if (records != null && records.length > 0)
        {
            System.out.println("    "+nameString+": "+l.getAnswers()[0].rdataToString()+" +"+(records.length-1));
            for(Record r : records)
            {
                if (r.getName().isWild() && !n.isWild())
                    r = r.withName(n);
                response.addRecord(r, Section.ANSWER);
            }
            put_cache(nameString, records);
            return true;
        }
        else
            System.out.println("    "+nameString+": "+l.getErrorString());
    }
    return false;
}

byte
addAnswer(Message response, Name name, int type, int dclass,
	  int iterations, int flags,InetAddress ip)
{
	SetResponse sr;
	byte rcode = Rcode.NOERROR;

	if (iterations > 6)
		return Rcode.NOERROR;

	if (type == Type.SIG || type == Type.RRSIG) {
		type = Type.ANY;
		flags |= FLAG_SIGONLY;
	}
        /*
	Zone zone = findBestZone(name);
	if (zone != null)
		sr = zone.findRecords(name, type);
	else {
		Cache cache = getCache(dclass);
		sr = cache.lookupRecords(name, type, Credibility.NORMAL);
	}
        
	if (sr.isUnknown()) {
		addCacheNS(response, getCache(dclass), name);
	}
	if (sr.isNXDOMAIN()) {
		response.getHeader().setRcode(Rcode.NXDOMAIN);
		if (zone != null) {
			addSOA(response, zone);
			if (iterations == 0)
				response.getHeader().setFlag(Flags.AA);
		}
		rcode = Rcode.NXDOMAIN;
	}
	else if (sr.isNXRRSET()) {
		if (zone != null) {
			addSOA(response, zone);
			if (iterations == 0)
				response.getHeader().setFlag(Flags.AA);
		}
	}
	else if (sr.isDelegation()) {
		RRset nsRecords = sr.getNS();
		addRRset(nsRecords.getName(), response, nsRecords,
			 Section.AUTHORITY, flags);
	}
	else if (sr.isCNAME()) {
		CNAMERecord cname = sr.getCNAME();
		RRset rrset = new RRset(cname);
		addRRset(name, response, rrset, Section.ANSWER, flags);
		if (zone != null && iterations == 0)
			response.getHeader().setFlag(Flags.AA);
		rcode = addAnswer(response, cname.getTarget(),
				  type, dclass, iterations + 1, flags);
	}
	else if (sr.isDNAME()) {
		DNAMERecord dname = sr.getDNAME();
		RRset rrset = new RRset(dname);
		addRRset(name, response, rrset, Section.ANSWER, flags);
		Name newname;
		try {
			newname = name.fromDNAME(dname);
		}
		catch (NameTooLongException e) {
			return Rcode.YXDOMAIN;
		}
		rrset = new RRset(new CNAMERecord(name, dclass, 0, newname));
		addRRset(name, response, rrset, Section.ANSWER, flags);
		if (zone != null && iterations == 0)
			response.getHeader().setFlag(Flags.AA);
		rcode = addAnswer(response, newname, type, dclass,
				  iterations + 1, flags);
	}
	else if (sr.isSuccessful()) {
		RRset [] rrsets = sr.answers();
		for (int i = 0; i < rrsets.length; i++)
			addRRset(name, response, rrsets[i],
				 Section.ANSWER, flags);
		if (zone != null) {
			addNS(response, zone, flags);
			if (iterations == 0)
				response.getHeader().setFlag(Flags.AA);
		}
		else
			addCacheNS(response, getCache(dclass), name);
	}*/
        
        // naive forward
        try{
            String nameString = DomainEntry.trimDomain(name.toString());
            
            if(domains_blacklist.contains(nameString))
            {
                printLog("## BLACKLIST BLOCKED: "+name+" ##");
                EmailSender.registerBlock(nameString,ip,interface_indentifier,room_name,"nome do site foi encontrado na blacklist",started_time,email);
                return Rcode.REFUSED;
                /*Record[] cached;
                if((cached = get_cache(dns_blocked)) != null)
                {
                    for(Record r : cached)
                    {
                        if (r.getName().isWild() && !name.isWild())
                            r = r.withName(name);
                        response.addRecord(r, Section.ANSWER);
                    }
                }
                else
                doDnsRequest(response, dns_blocked);*/
            }
            
            for(String word : domains_blacklist_words)
            {
                if(nameString.contains(word)) 
                {
                    printLog("## WORD BLOCKED: "+nameString+" contains:'"+word+"' ##");
                    EmailSender.registerBlock(nameString,ip,interface_indentifier,room_name,"nome do site contém a palavra '"+word+"'",started_time,email);
                    regex_blocked_log(nameString);
                    return Rcode.REFUSED;
                }
            }
            
            for(String regex : domains_blacklist_regex)
            {
                if(nameString.matches(regex))
                {
                    printLog("## REGEX BLOCKED: "+nameString+" matches:'"+regex+"' ##");
                    EmailSender.registerBlock(nameString,ip,interface_indentifier,room_name,"nome do site combina com a regra <i>regex</i>: '"+regex+"'",started_time,email);
                    regex_blocked_log(nameString);
                    return Rcode.REFUSED;
                }
            }

            Record[] cached;
            if((cached = get_cache(nameString)) != null)
            {
                printLog("    "+nameString+": "+cached[0].rdataToString()+" +"+(cached.length-1));
                for(Record r : cached)
                {
                    if (r.getName().isWild() && !name.isWild())
                        r = r.withName(name);
                    response.addRecord(r, Section.ANSWER);
                }
            }
            else
            {
                Lookup l = new Lookup(name);//, Type.TXT, DClass.CH);
                l.setResolver(new SimpleResolver(dns_server));
                for(int i=0;i<3;i++)
                {
                    Record[] records = l.run();
                    if (records != null && records.length > 0)
                    {
                        printLog("!   "+nameString+": "+l.getAnswers()[0].rdataToString()+" +"+(records.length-1));
                        for(Record r : records)
                        {
                            if (r.getName().isWild() && !name.isWild())
                                r = r.withName(name);
                            response.addRecord(r, Section.ANSWER);
                        }
                        put_cache(nameString, records);
                        break;
                    }
                    else
                        printLog("!   "+nameString+": "+l.getErrorString());
                }
            }
            
            
        } catch(Exception e){e.printStackTrace();}
        
	return rcode;
}

byte []
doAXFR(Name name, Message query, TSIG tsig, TSIGRecord qtsig, Socket s) {
	Zone zone = (Zone) znames.get(name);
	boolean first = true;
	if (zone == null)
		return errorMessage(query, Rcode.REFUSED);
	Iterator it = zone.AXFR();
	try {
		DataOutputStream dataOut;
		dataOut = new DataOutputStream(s.getOutputStream());
		int id = query.getHeader().getID();
		while (it.hasNext()) {
			RRset rrset = (RRset) it.next();
			Message response = new Message(id);
			Header header = response.getHeader();
			header.setFlag(Flags.QR);
			header.setFlag(Flags.AA);
			addRRset(rrset.getName(), response, rrset,
				 Section.ANSWER, FLAG_DNSSECOK);
			if (tsig != null) {
				tsig.applyStream(response, qtsig, first);
				qtsig = response.getTSIG();
			}
			first = false;
			byte [] out = response.toWire();
			dataOut.writeShort(out.length);
			dataOut.write(out);
		}
	}
	catch (IOException ex) {
		System.out.println("AXFR failed");
	}
	try {
		s.close();
	}
	catch (IOException ex) {
	}
	return null;
}

/*
 * Note: a null return value means that the caller doesn't need to do
 * anything.  Currently this only happens if this is an AXFR request over
 * TCP.
 */
byte []
generateReply(Message query, byte [] in, int length, Socket s,InetAddress ip)
throws IOException
{
	Header header;
	boolean badversion;
	int maxLength;
	int flags = 0;

	header = query.getHeader();
	if (header.getFlag(Flags.QR))
		return null;
	if (header.getRcode() != Rcode.NOERROR)
		return errorMessage(query, Rcode.FORMERR);
	if (header.getOpcode() != Opcode.QUERY)
		return errorMessage(query, Rcode.NOTIMP);

	Record queryRecord = query.getQuestion();

	TSIGRecord queryTSIG = query.getTSIG();
	TSIG tsig = null;
	if (queryTSIG != null) {
		tsig = (TSIG) TSIGs.get(queryTSIG.getName());
		if (tsig == null ||
		    tsig.verify(query, in, length, null) != Rcode.NOERROR)
			return formerrMessage(in);
	}

	OPTRecord queryOPT = query.getOPT();
	if (queryOPT != null && queryOPT.getVersion() > 0)
		badversion = true;

	if (s != null)
		maxLength = 65535;
	else if (queryOPT != null)
		maxLength = Math.max(queryOPT.getPayloadSize(), 512);
	else
		maxLength = 512;

	if (queryOPT != null && (queryOPT.getFlags() & ExtendedFlags.DO) != 0)
		flags = FLAG_DNSSECOK;

	Message response = new Message(query.getHeader().getID());
	response.getHeader().setFlag(Flags.QR);
	if (query.getHeader().getFlag(Flags.RD))
		response.getHeader().setFlag(Flags.RD);
	response.addRecord(queryRecord, Section.QUESTION);

	Name name = queryRecord.getName();
	int type = queryRecord.getType();
	int dclass = queryRecord.getDClass();
	if (type == Type.AXFR && s != null)
		return doAXFR(name, query, tsig, queryTSIG, s);
	if (!Type.isRR(type) && type != Type.ANY)
		return errorMessage(query, Rcode.NOTIMP);

        //System.out.print("Resolving "+name.toString()+":");
        
	byte rcode = addAnswer(response, name, type, dclass, 0, flags,ip);
	if (rcode != Rcode.NOERROR && rcode != Rcode.NXDOMAIN)
		return errorMessage(query, rcode);

	addAdditional(response, flags);

	if (queryOPT != null) {
		int optflags = (flags == FLAG_DNSSECOK) ? ExtendedFlags.DO : 0;
		OPTRecord opt = new OPTRecord((short)4096, rcode, (byte)0,
					      optflags);
		response.addRecord(opt, Section.ADDITIONAL);
	}

	response.setTSIG(tsig, Rcode.NOERROR, queryTSIG);
	return response.toWire(maxLength);
}

byte []
buildErrorMessage(Header header, int rcode, Record question) {
	Message response = new Message();
	response.setHeader(header);
	for (int i = 0; i < 4; i++)
		response.removeAllRecords(i);
	if (rcode == Rcode.SERVFAIL)
		response.addRecord(question, Section.QUESTION);
	header.setRcode(rcode);
	return response.toWire();
}

public byte []
formerrMessage(byte [] in) {
	Header header;
	try {
		header = new Header(in);
	}
	catch (IOException e) {
		return null;
	}
	return buildErrorMessage(header, Rcode.FORMERR, null);
}

public byte []
errorMessage(Message query, int rcode) {
	return buildErrorMessage(query.getHeader(), rcode,
				 query.getQuestion());
}

public void
TCPclient(Socket s) {
	try {
		int inLength;
		DataInputStream dataIn;
		DataOutputStream dataOut;
		byte [] in;

		InputStream is = s.getInputStream();
		dataIn = new DataInputStream(is);
		inLength = dataIn.readUnsignedShort();
		in = new byte[inLength];
		dataIn.readFully(in);

		Message query;
		byte [] response = null;
		try {
			query = new Message(in);
			response = generateReply(query, in, in.length, s,s.getLocalAddress());
			if (response == null)
				return;
		}
		catch (IOException e) {
			response = formerrMessage(in);
		}
		dataOut = new DataOutputStream(s.getOutputStream());
		dataOut.writeShort(response.length);
		dataOut.write(response);
	}
	catch (IOException e) {
            e.printStackTrace();
		System.out.println("TCPclient(" +
				   addrport(s.getLocalAddress(),
					    s.getLocalPort()) +
				   "): " + e);
	}
	finally {
		try {
			s.close();
		}
		catch (IOException e) {}
	}
}

public void
UDPclient(byte[] in, int indp_length, InetAddress address, int port)
{
    try {
        Message query;
        byte [] response = null;
        try {
            query = new Message(in);
            response = generateReply(query, in,
                    indp_length,
                    null,address);
            if (response == null)
                return; // continue;
        }
        catch (IOException e) {
            response = formerrMessage(in);
        }
        //if (outdp == null)
        DatagramPacket outdp = new DatagramPacket(response,
                response.length,
                address,
                port);
        //else {
        //        outdp.setData(response);
        //        outdp.setLength(response.length);
        //        outdp.setAddress(indp.getAddress());
        //        outdp.setPort(indp.getPort());
        //}
        DatagramSocket sock = new DatagramSocket();
        sock.send(outdp);
    }
    catch (SocketException ex) {
        ex.printStackTrace();
    } catch (IOException ex) {
        ex.printStackTrace();
    }
}

public void
serveTCP(InetAddress addr, int port) {
    while (true) {
	try {
		ServerSocket sock = new ServerSocket(port, 128, addr);
		while (true) {
			final Socket s = sock.accept();
			Thread t;
			t = new Thread(new Runnable() {
					public void run() {TCPclient(s);}});
			t.start();
		}
                
	}
	catch (Exception e) {
            e.printStackTrace();
		System.out.println("serveTCP(" + addrport(addr, port) + "): " +
				   e);
	}
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(jnamed.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

public void
serveUDP(InetAddress addr, int port) {
        while (true) {
	try {
		DatagramSocket sock = new DatagramSocket(port, addr);
		final short udpLength = 512;
		//byte [] in = new byte[udpLength];
		
		DatagramPacket outdp = null;
		while (true) {
                        final byte [] in = new byte[udpLength];
                        DatagramPacket indp = new DatagramPacket(in, in.length);
                        
			indp.setLength(in.length);
			try {
				sock.receive(indp);
			}
			catch (InterruptedIOException e) {
				continue;
			}
                        final int indp_length = indp.getLength();
                        final InetAddress indp_address = indp.getAddress();
                        final int indp_port = indp.getPort();
                        Thread t = new Thread(new Runnable() {
					public void run() {UDPclient(in,indp_length,indp_address,indp_port);}});
			t.start();
			/*Message query;
			byte [] response = null;
			try {
				query = new Message(in);
				response = generateReply(query, in,
							 indp.getLength(),
							 null);
				if (response == null)
					continue;
			}
			catch (IOException e) {
				response = formerrMessage(in);
			}
			if (outdp == null)
				outdp = new DatagramPacket(response,
							   response.length,
							   indp.getAddress(),
							   indp.getPort());
			else {
				outdp.setData(response);
				outdp.setLength(response.length);
				outdp.setAddress(indp.getAddress());
				outdp.setPort(indp.getPort());
			}
			sock.send(outdp);*/
		}
	}
	catch (Exception e) {
            e.printStackTrace();
		System.out.println("serveUDP(" + addrport(addr, port) + "): " +
				   e);
	}
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(jnamed.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
}

public void
addTCP(final InetAddress addr, final int port) {
	Thread t;
	t = new Thread(new Runnable() {
			public void run() {serveTCP(addr, port);}});
	t.start();
}

public void
addUDP(final InetAddress addr, final int port) {
	Thread t;
	t = new Thread(new Runnable() {
			public void run() {serveUDP(addr, port);}});
	t.start();
}

public static void main(String [] args) {
	/*if (args.length > 1) {
		System.out.println("usage: jnamed [conf]");
		System.exit(0);
	}*/
        /*try{
            File log_folder = new File(System.getenv("appdata")+ "\\dnsproxy\\");
            log_folder.mkdirs();
            
            LocalDateTime now = LocalDateTime.now();
            String format3 = now.format(DateTimeFormatter.ofPattern("HH-mm-ss dd-MM-yyyy", Locale.getDefault()));

            File logFile = new File(log_folder,"log "+format3+".txt");
            //old_out = System.out;
            PrintStream new_out = new PrintStream(new BufferedOutputStream(new FileOutputStream(logFile)),true,"UTF-8");
            System.setOut(new_out);
            System.setErr(new_out);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }*/
        
        //while(true)
        //{
            jnamed s;
            try {
                    String conf;
                    //if (args.length == 1)
                    //	conf = args[0];
                    //else
                    conf = "jnamed.conf";
                    // teste
                    s = new jnamed(conf);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        //}
}

}
