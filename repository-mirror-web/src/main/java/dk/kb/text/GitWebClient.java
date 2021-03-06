package dk.kb.text;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitWebClient {

	private static ConfigurableConstants consts = ConfigurableConstants.getInstance();
	private static Logger logger = Logger.getLogger(GitWebClient.class);

	private Git git_client = null;

	private CredentialsProvider credentials = null;

	public GitWebClient() {
		String repo = "public-adl-text-sources";
		init(repo);
	}

	public GitWebClient(String repo) {
		init(repo);
	}

	private void init(String repo) {
		String home   = consts.getConstants().getProperty("data.home");
		String user   = consts.getConstants().getProperty("git.user");
		String passwd = consts.getConstants().getProperty("git.password");

		try {
			git_client = Git.open( new F‌ile( home + "/" + repo + "/.git" ) );
		} catch(java.io.IOException repoProblem ) {
			logger.error("git IO prob: " + repoProblem);
		}
		credentials = new UsernamePasswordCredentialsProvider(user,passwd);
	}

        public void close() {
	    git_client.close();
	    
	}

	public String gitLog() {
		try {
			LogCommand log = git_client.log();
			//	    log.setCredentialsProvider(credentials);
			java.lang.Iterable<RevCommit> log_arator = log.call();
			return log_arator.iterator().next().toString();
		} catch (org.eclipse.jgit.api.errors.GitAPIException gitProblem) {
			logger.error("git log prob: " + gitProblem);
			return "git failed";
		}
	}


	//    cloneCommand.setCredentialsProvider( new UsernamePasswordCredentialsProvider( "user", "password" ) );

	public String gitFetch() {
		try {
			FetchCommand fetch = git_client.fetch();
			fetch.setCredentialsProvider(credentials);
			fetch.setRemoveDeletedRefs(true);
			FetchResult res = fetch.call();
			return res.toString();
		} catch (org.eclipse.jgit.api.errors.GitAPIException gitProblem) {
			logger.error("git fetch prob: " + gitProblem);
			return "git failed";
		}
	}

	public String gitPull() {
		try {
			PullCommand pull = git_client.pull();
			pull.setCredentialsProvider(credentials);
			PullResult res   = pull.call();
			return res.toString();
		} catch (org.eclipse.jgit.api.errors.GitAPIException gitProblem) {
			logger.error("git pull prob: " + gitProblem);
			return "git failed";
		}
	}

	public String gitBranches() {
		try {
			ListBranchCommand branches = git_client.branchList();
			//	    branches.setCredentialsProvider(credentials);
			branches.setListMode(ListBranchCommand.ListMode.ALL);
			java.util.List<Ref> res      = branches.call();
			Iterator<Ref> lister =  res.iterator();
			String blist = "";
			while(lister.hasNext()) {
				Ref branch = lister.next();
				String name = branch.getName() + "";

				Pattern pat   = Pattern.compile("^refs/remotes.*");
				// match anything
				// Pattern pat   = Pattern.compile(".*");
				Matcher match = pat.matcher(name);

				String rgx = "^(.*?)/([^/]*)$";
				String readable_name  = name.replaceAll(rgx,"$2");

				if(match.matches()) {
					String item = "<option value=" + name + ">" + readable_name + "</option>\n";
					blist = blist + item;
				}
			}
			return blist;
		} catch (org.eclipse.jgit.api.errors.GitAPIException gitProblem) {
			logger.error("git branches prob: " + gitProblem);
			return "git failed";
		}
	}

}
