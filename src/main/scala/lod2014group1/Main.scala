package lod2014group1
import lod2014group1.crawling.Crawler
import org.slf4s.Logging
import lod2014group1.apis._
import lod2014group1.triplification.Triplifier
import java.io.File
import lod2014group1.rdf._
import org.joda.time.DateTime
import lod2014group1.rdf.RdfMovieResource._
import lod2014group1.amqp._
import lod2014group1.amqp.WorkerTask

object Main extends App with Logging {

	override def main(args: Array[String]): Unit = {
		log.debug("Started.")
		log.info("Arguments: " + args.toList)
		if (args contains "triplify") {
			Triplifier.go
		} else if (args contains "crawl-imdb") {
			Crawler.crawl
		} else if (args contains "crawl-tmdb") {
    		val tmdb = new lod2014group1.crawling.TMDBMoviesListCrawler()
    		tmdb.crawl
		} else if (args contains "rabbit-worker") {
			val worker = new WorkReceiver("tasks", "answers")
			worker.listen()
		} else if (args contains "rabbit-server") {
			new Thread(new RPCServer("answers")).start();
			val crawlTask1 = WorkerTask(TaskType.Crawl.toString, Map("uri" -> "http://Kung_Fu_Panda", "task_id" -> "1", "content" -> FileContent.longString))
			val triplifyTask1 = WorkerTask(TaskType.Triplify.toString, Map("uri" -> "Kung_Fu_Panda", "task_id" -> "2", "content" -> FileContent.longString))
			val crawlTask2 = WorkerTask(TaskType.Crawl.toString, Map("uri" -> "http://Fight_Club", "task_id" -> "3", "content" -> FileContent.longString))
			val triplifyTask2 = WorkerTask(TaskType.Triplify.toString, Map("uri" -> "Fight_Club", "task_id" -> "4", "content" -> FileContent.longString))
			val crawlTask3 = WorkerTask(TaskType.Crawl.toString, Map("uri" -> "http://Godzilla", "task_id" -> "5", "content" -> FileContent.longString))
			val triplifyTask3 = WorkerTask(TaskType.Triplify.toString, Map("uri" -> "Godzilla", "task_id" -> "6", "content" -> FileContent.longString))
			val crawlTask4 = WorkerTask(TaskType.Crawl.toString, Map("uri" -> "http://August_Rush", "task_id" -> "7", "content" -> FileContent.longString))
			val triplifyTask4 = WorkerTask(TaskType.Triplify.toString, Map("uri" -> "August_Rush", "task_id" -> "8", "content" -> FileContent.longString))
			val dummyTask = WorkerTask(null, Map())
			val sup = new Supervisor("tasks")

			(0 to 1000).foreach { _ =>
				sup.send(crawlTask1)
				sup.send(crawlTask2)
				sup.send(crawlTask3)
				sup.send(crawlTask4)
				sup.send(triplifyTask1)
				sup.send(triplifyTask2)
				sup.send(triplifyTask3)
				sup.send(triplifyTask4)
				sup.send(dummyTask)
			}
		} else if (args contains "crawl-ofdb") {
			val ofdb = new lod2014group1.crawling.OFDBMovieCrawler()
			ofdb.crawl
		} else if (args contains "freebase") {
		  val freebase = new lod2014group1.crawling.FreebaseFilmCrawler()
		  //freebase.getAllNotImdbMovies
		  //freebase.getFreebaseFilmsWithIMDB
		  //freebase.getExampleRdf
		  //freebase.loadAllFilmId
		  freebase.crawl
		} else if (args contains "dbpedia") {
			val dbpedia = new DBpediaAPI()
			dbpedia getAllTriplesFor "http://dbpedia.org/resource/Despicable_Me"
		} else if (args contains "ofdb-coverage"){
			val ofdb = new lod2014group1.crawling.OFDBMovieCrawler()
			ofdb.coverage
		} else {
			log.warn("Please pass a parameter to indicate what you want to do, e.g. run `gradle crawl` or `gradle triplify`.")
		}
		log.debug("Finished.")
	}
}

object FileContent {
	val longString =
		"""
		  |
		  |
		  |
		  |
		  |
		  |
		  |
		  |<!DOCTYPE html>
		  |<html
		  |xmlns:og="http://ogp.me/ns#"
		  |xmlns:fb="http://www.facebook.com/2008/fbml">
		  |    <head>
		  |            <script type="text/javascript">var ue_t0=window.ue_t0||+new Date();</script>
		  |            <script type="text/javascript">
		  |                var ue_mid = "A1EVAM02EL8SFB"; 
		  |                var ue_sn = "www.imdb.com";  
		  |                var ue_furl = "fls-na.amazon.com";
		  |                var ue_sid = "845-8786150-9672452";
		  |                var ue_id = "1AS3C1K4E3G5KR01FPMR";
		  |                (function(e){var c=e,a={main_scope:"mainscopecsm",q:[],t0:c.ue_t0||+new Date(),d:g};function g(h){return +new Date()-(h?0:a.t0)}function d(h){return function(){a.q.push({n:h,a:arguments,t:a.d()})}}function b(k,j,h){var i={m:k,f:j,l:h,fromOnError:1,args:arguments};c.ueLogError(i);return false}b.skipTrace=1;e.onerror=b;function f(){c.uex("ld")}if(e.addEventListener){e.addEventListener("load",f,false)}else{if(e.attachEvent){e.attachEvent("onload",f)}}a.tag=d("tag");a.log=d("log");a.reset=d("rst");c.ue_csm=c;c.ue=a;c.ueLogError=d("err");c.ues=d("ues");c.uet=d("uet");c.uex=d("uex");c.uet("ue")})(window);(function(e,d){var a=e.ue||{};function c(g){if(!g){return}var f=d.head||d.getElementsByTagName("head")[0]||d.documentElement,h=d.createElement("script");h.async="async";h.src=g;f.insertBefore(h,f.firstChild)}function b(){var k=e.ue_cdn||"z-ecx.images-amazon.com",g=e.ue_cdns||"images-na.ssl-images-amazon.com",j="/images/G/01/csminstrumentation/",h=e.ue_file||"ue-full-ef584a44e8ea58e3d4d928956600a9b6._V1_.js",f,i;if(h.indexOf("NSTRUMENTATION_FIL")>=0){return}if("ue_https" in e){f=e.ue_https}else{f=e.location&&e.location.protocol=="https:"?1:0}i=f?"https://":"http://";i+=f?g:k;i+=j;i+=h;c(i)}if(!e.ue_inline){b()}a.uels=c;e.ue=a})(window,document);
		  |            </script>
		  |
		  |
		  |        
		  |        <script type="text/javascript">var IMDbTimer={starttime: new Date().getTime(),pt:'java'};</script>
		  |        
		  |  <script>(function(t){ (t.events = t.events || {})["csm_head_pre_title"] = new Date().getTime(); })(IMDbTimer);</script>
		  |        <title>Kung Fu Panda (2008) - IMDb</title>
		  |  <script>(function(t){ (t.events = t.events || {})["csm_head_post_title"] = new Date().getTime(); })(IMDbTimer);</script>
		  |        
		  |        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		  |            <link rel="canonical" href="http://www.imdb.com/title/tt0441773/" />
		  |            <meta property="og:url" content="http://www.imdb.com/title/tt0441773/" />
		  |        
		  |  <script>(function(t){ (t.events = t.events || {})["csm_head_pre_icon"] = new Date().getTime(); })(IMDbTimer);</script>
		  |        <link rel="icon" type="image/ico" href="http://ia.media-imdb.com/images/G/01/imdb/images/favicon-2165806970._V379387995_.ico" />
		  |        <link rel="shortcut icon" type="image/x-icon" href="http://ia.media-imdb.com/images/G/01/imdb/images/desktop-favicon-2165806970._V379390718_.ico" />
		  |        <link href="http://ia.media-imdb.com/images/G/01/imdb/images/mobile/apple-touch-icon-web-4151659188._V361295786_.png" rel="apple-touch-icon"> 
		  |        <link href="http://ia.media-imdb.com/images/G/01/imdb/images/mobile/apple-touch-icon-web-76x76-53536248._V361295462_.png" rel="apple-touch-icon" sizes="76x76"> 
		  |        <link href="http://ia.media-imdb.com/images/G/01/imdb/images/mobile/apple-touch-icon-web-120x120-2442878471._V361295428_.png" rel="apple-touch-icon" sizes="120x120"> 
		  |        <link href="http://ia.media-imdb.com/images/G/01/imdb/images/mobile/apple-touch-icon-web-152x152-1475823641._V361295368_.png" rel="apple-touch-icon" sizes="152x152"> 
		  |        <link rel="search" type="application/opensearchdescription+xml" href="http://ia.media-imdb.com/images/G/01/imdb/images/imdbsearch-3349468880._V379388505_.xml" title="IMDb" />
		  |  <script>(function(t){ (t.events = t.events || {})["csm_head_post_icon"] = new Date().getTime(); })(IMDbTimer);</script>
		  |        
		  |    
		  |        <link rel='image_src' href="http://ia.media-imdb.com/images/M/MV5BMTIxOTY1NjUyN15BMl5BanBnXkFtZTcwMjMxMDk1MQ@@._V1_SY755_SX483_AL_.jpg">
		  |        <meta property='og:image' content="http://ia.media-imdb.com/images/M/MV5BMTIxOTY1NjUyN15BMl5BanBnXkFtZTcwMjMxMDk1MQ@@._V1_SY755_SX483_AL_.jpg" />
		  |    
		  |        <meta property='og:type' content="video.movie" />
		  |    <meta property='fb:app_id' content='115109575169727' />
		  |    <meta property='og:title' content="Kung Fu Panda (2008)" />
		  |    <meta property='og:site_name' content='IMDb' />
		  |    <meta name="title" content="Kung Fu Panda (2008) - IMDb" />
		  |        <meta name="description" content="Directed by Mark Osborne, John Stevenson.  With Jack Black, Ian McShane, Angelina Jolie, Dustin Hoffman. In the Valley of Peace, Po the Panda finds himself chosen as the Dragon Warrior despite the fact that he is obese and a complete novice at martial arts." />
		  |        <meta property="og:description" content="Directed by Mark Osborne, John Stevenson.  With Jack Black, Ian McShane, Angelina Jolie, Dustin Hoffman. In the Valley of Peace, Po the Panda finds himself chosen as the Dragon Warrior despite the fact that he is obese and a complete novice at martial arts." />
		  |        <meta name="keywords" content="Reviews, Showtimes, DVDs, Photos, Message Boards, User Ratings, Synopsis, Trailers, Credits" />
		  |        <meta name="request_id" content="1AS3C1K4E3G5KR01FPMR" />
		  |        
		  |  <script>(function(t){ (t.events = t.events || {})["csm_head_pre_css"] = new Date().getTime(); })(IMDbTimer);</script>
		  |<!-- h=ics-http-1a-c1xl-i-d30356f2.us-east-1 -->
		  |
		  |            <link rel="stylesheet" type="text/css" href="http://ia.media-imdb.com/images/G/01/imdb/css/collections/title-2250810375._V336031960_.css" />
		  |            <!--[if IE]><link rel="stylesheet" type="text/css" href="http://ia.media-imdb.com/images/G/01/imdb/css/collections/ie-1918465287._V354866480_.css" /><![endif]-->
		  |            <link rel="stylesheet" type="text/css" href="http://ia.media-imdb.com/images/G/01/imdb/css/site/consumer-navbar-mega-3538633082._V355276482_.css" />
		  |        <noscript>
		  |            <link rel="stylesheet" type="text/css" href="http://ia.media-imdb.com/images/G/01/imdb/css/wheel/nojs-2627072490._V343672767_.css">
		  |        </noscript>
		  |  <script>(function(t){ (t.events = t.events || {})["csm_head_post_css"] = new Date().getTime(); })(IMDbTimer);</script>
		  |        
		  |  <script>(function(t){ (t.events = t.events || {})["csm_head_pre_ads"] = new Date().getTime(); })(IMDbTimer);</script>
		  |        <script>
		  |window.ads_js_start = new Date().getTime();
		  |var imdbads = imdbads || {}; imdbads.cmd = imdbads.cmd || [];
		  |</script>
		  |<!-- begin SRA -->
		  |<script>
		  |(function(){var d=function(o){return Object.prototype.toString.call(o)==="[object Array]";},g=function(q,p){var o;for(o=0;o<q.length;o++){if(o in q){p.call(null,q[o],o);}}},h=[],k,b,l=false,n=false,f=function(){var o=[],p=[],q={};g(h,function(s){var r="";g(s.dartsite.split("/"),function(t){if(t!==""){if(t in q){}else{q[t]=o.length;o.push(t);}r+="/"+q[t];}});p.push(r);});return{iu_parts:o,enc_prev_ius:p};},c=function(){var o=[];g(h,function(q){var p=[];g(q.sizes,function(r){p.push(r.join("x"));});o.push(p.join("|"));});return o;},m=function(){var o=[];g(h,function(p){o.push(a(p.targeting));});return o.join("|");},a=function(r,o){var q,p,s=[];for(q in r){p=[];for(j=0;j<r[q].length;j++){p.push(encodeURIComponent(r[q][j]));}if(o){s.push(q+"="+encodeURIComponent(p.join(",")));}else{s.push(q+"="+p.join(","));}}return s.join("&");},e=function(){var o=+new Date();if(n){return;}if(!this.readyState||"loaded"===this.readyState){n=true;if(l){imdbads.cmd.push(function(){for(i=0;i<h.length;i++){generic.monitoring.record_metric(h[i].name+".fail",csm.duration(o));}});}}};window.tinygpt={define_slot:function(r,q,o,p){h.push({dartsite:r.replace(/\/$/,""),sizes:q,name:o,targeting:p});},set_targeting:function(o){k=o;},callback:function(p){var q,o={},s,r=+new Date();l=false;for(q=0;q<h.length;q++){s=h[q].dartsite;name=h[q].name;if(p[q][s]){o[name]=p[q][s];}else{window.console&&console.error&&console.error("Unable to correlate GPT response for "+name);}}imdbads.cmd.push(function(){for(q=0;q<h.length;q++){ad_utils.slot_events.trigger(h[q].name,"request",{timestamp:b});ad_utils.slot_events.trigger(h[q].name,"tagdeliver",{timestamp:r});}ad_utils.gpt.handle_response(o);});},send:function(){var r=[],q=function(s,t){if(d(t)){t=t.join(",");}if(t){r.push(s+"="+encodeURIComponent(""+t));}},o,p;if(h.length===0){tinygpt.callback({});return;}q("gdfp_req","1");q("correlator",Math.floor(4503599627370496*Math.random()));q("output","json_html");q("callback","tinygpt.callback");q("impl","fifs");q("json_a","1");result=f();q("iu_parts",result.iu_parts);q("enc_prev_ius",result.enc_prev_ius);q("prev_iu_szs",c());q("prev_scp",m());q("cust_params",a(k,true));o=document.createElement("script");p=document.getElementsByTagName("script")[0];o.async=true;o.type="text/javascript";o.src="http://pubads.g.doubleclick.net/gampad/ads?"+r.join("&");o.id="tinygpt";o.onload=o.onerror=o.onreadystatechange=e;l=true;p.parentNode.insertBefore(o,p);b=+new Date();}};})();</script>
		  |<script>
		  |tinygpt.define_slot('/4215/imdb2.consumer.title/maindetails',
		  |[[300,250],[16,1]],
		  |'btf_rhs2',
		  |{
		  |'p': ['br2']
		  |});
		  |tinygpt.define_slot('/4215/imdb2.consumer.title/maindetails',
		  |[[728,90],[2,1]],
		  |'bottom_ad',
		  |{
		  |'p': ['b']
		  |});
		  |tinygpt.define_slot('/4215/imdb2.consumer.title/maindetails',
		  |[[300,250],[300,600],[300,300],[11,1]],
		  |'top_rhs',
		  |{
		  |'p': ['tr']
		  |});
		  |tinygpt.define_slot('/4215/imdb2.consumer.title/maindetails',
		  |[[728,90],[1008,150],[1008,200],[1008,30],[970,250],[1008,400],[9,1]],
		  |'top_ad',
		  |{
		  |'p': ['top','t']
		  |});
		  |tinygpt.set_targeting({
		  |'g' : ['fm','co','an','ac','ad','baa'],
		  |'tt' : ['f'],
		  |'m' : ['PG'],
		  |'mh' : ['PG'],
		  |'ml' : ['PG'],
		  |'coo' : ['us'],
		  |'b' : [],
		  |'fv' : ['1'],
		  |'id' : ['tt0441773'],
		  |'ab' : ['c'],
		  |'bpx' : ['2'],
		  |'md' : ['tt0441773'],
		  |'s' : ['3075','32','1009','3717'],
		  |'u': ['065068742621'],
		  |'oe': ['utf-8']
		  |});
		  |tinygpt.send();
		  |</script>
		  |<!-- begin ads header -->
		  |<script src="http://ia.media-imdb.com/images/G/01/imdbads/js/collections/ads-1549802990._V336840965_.js"></script>
		  |<script>
		  |doWithAds = function(){};
		  |</script>
		  |<script>
		  |doWithAds = function(inside, failureMessage){
		  |if ('consoleLog' in window &&
		  |'generic' in window &&
		  |'ad_utils' in window &&
		  |'custom' in window &&
		  |'monitoring' in generic &&
		  |'document_is_ready' in generic) {
		  |try{
		  |inside.call(this);
		  |}catch(e) {
		  |if ( window.ueLogError ) {
		  |if(typeof failureMessage !== 'undefined'){
		  |e.message = failureMessage;
		  |}
		  |e.attribution = "Advertising";
		  |e.logLevel = "ERROR";
		  |ueLogError(e);
		  |}
		  |if( (document.location.hash.match('debug=1')) &&
		  |(typeof failureMessage !== 'undefined') ){
		  |console.error(failureMessage);
		  |}
		  |}
		  |} else {
		  |if( (document.location.hash.match('debug=1')) &&
		  |(typeof failureMessage !== 'undefined') ){
		  |console.error(failureMessage);
		  |}
		  |}
		  |};
		  |</script><script>
		  |doWithAds(function(){
		  |generic.monitoring.record_metric("ads_js_request_to_done", (new Date().getTime()) - window.ads_js_start);
		  |ad_utils.weblab.set_treatment('gpt single-request', 'Use GPT ad requests.');
		  |generic.monitoring.enable_weblab_metrics('107', '2', [
		  |'csm_core_ads_load', 'csm_core_ads_iframe', 'csm_core_ads_reflow', 'csm_core_ads_tagdeliver', 'csm_core_ads_request',
		  |'csm_top_ad_load', 'csm_top_ad_iframe', 'csm_top_ad_reflow', 'csm_top_ad_tagdeliver', 'csm_top_ad_request',
		  |'csm_top_rhs_load', 'csm_top_rhs_iframe', 'csm_top_rhs_reflow', 'csm_top_rhs_tagdeliver', 'csm_top_rhs_request',
		  |'top_ad.got_ad', 'top_rhs.got_ad', 'injected_billboard.got_ad', 'injected_navstrip.got_ad', 'bottom_ad.got_ad',
		  |'top_ad.blank', 'top_rhs.blank', 'injected_billboard.blank', 'injected_navstrip.blank', 'bottom_ad.blank',
		  |'top_ad.null', 'top_rhs.null', 'injected_billboard.null', 'injected_navstrip.null', 'bottom_ad.null',
		  |'page_load'
		  |]);
		  |generic.monitoring.set_forester_info("title");
		  |generic.monitoring.set_twilight_info(
		  |"title",
		  |"DE",
		  |"a39ee5f6382792042ec6e79b59e5ab257823bb96",
		  |"2014-05-21T16%3A53%3A16GMT",
		  |"http://s.media-imdb.com/twilight/?",
		  |"consumer");
		  |generic.send_csm_head_metrics && generic.send_csm_head_metrics();
		  |generic.monitoring.start_timing("page_load");
		  |generic.seconds_to_midnight = 50804;
		  |generic.days_to_midnight = 0.588009238243103;
		  |ad_utils.set_slots_on_page({ 'injected_navstrip':1, 'top_ad':1, 'top_rhs':1, 'navboard':1, 'btf_rhs2':1, 'bottom_ad':1, 'injected_billboard':1, 'rhs_cornerstone':1 });
		  |custom.full_page.data_url = "http://ia.media-imdb.com/images/G/01/imdbads/js/graffiti_data-1150089811._V336142543_.js";
		  |consoleLog('advertising initialized','ads');
		  |},"ads js missing, skipping ads setup.");
		  |var _gaq = _gaq || [];
		  |_gaq.push(['_setCustomVar', 4, 'ads_abtest_treatment', 'c']);
		  |</script>
		  |<script>doWithAds(function() { ad_utils.ads_header.done(); });</script>
		  |<!-- end ads header -->
		  |        <script  type="text/javascript">
		  |            // ensures js doesn't die if ads service fails.  
		  |            // Note that we need to define the js here, since ad js is being rendered inline after this.
		  |            (function(f) {
		  |                // Fallback javascript, when the ad Service call fails.  
		  |                
		  |                if((window.csm === undefined || window.generic === undefined || window.consoleLog === undefined)) {
		  |                    if (console !== undefined && console.log !== undefined) {
		  |                        console.log("one or more of window.csm, window.generic or window.consoleLog has been stubbed...");
		  |                    }
		  |                }
		  |                
		  |                window.csm = window.csm || { measure:f, record:f, duration:f, listen:f, metrics:{} };
		  |                window.generic = window.generic || { monitoring: { start_timing: f, stop_timing: f } };
		  |                window.consoleLog = window.consoleLog || f;
		  |            })(function() {});
		  |        </script>
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_head_delivery_finished');
		  |    }
		  |  </script>
		  |        </head>
		  |    <body id="styleguide-v2" class="fixed">
		  |<script>
		  |    if (typeof uet == 'function') {
		  |      uet("bb");
		  |    }
		  |</script>
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_body_delivery_started');
		  |    }
		  |  </script>
		  |        <div id="wrapper">
		  |            <div id="root" class="redesign">
		  |<script>
		  |    if (typeof uet == 'function') {
		  |      uet("ns");
		  |    }
		  |</script>
		  |<div id="nb20" class="navbarSprite">
		  |<div id="supertab">	
		  |	<!-- begin TOP_AD -->
		  |<div id="top_ad_wrapper" class="dfp_slot">
		  |<script type="text/javascript">
		  |doWithAds(function(){
		  |ad_utils.register_ad('top_ad');
		  |});
		  |</script>
		  |<iframe data-dart-params="#imdb2.consumer.title/maindetails;!TILE!;sz=728x90,1008x150,1008x200,1008x30,970x250,1008x400,9x1;p=top;p=t;g=fm;g=co;g=an;g=ac;g=ad;g=baa;tt=f;m=PG;mh=PG;ml=PG;coo=us;fv=1;id=tt0441773;ab=c;bpx=2;md=tt0441773;s=3075;s=32;s=1009;s=3717;oe=utf-8;[CLIENT_SIDE_KEYVALUES];u=065068742621;ord=065068742621?" id="top_ad" name="top_ad" class="yesScript" width="0" height="0" data-original-width="0" data-original-height="0" data-config-width="0" data-config-height="0" data-cookie-width="null" data-cookie-height="null" marginwidth="0" marginheight="0" frameborder="0" scrolling="no" allowtransparency="true" onload="doWithAds.call(this, function(){ ad_utils.on_ad_load(this); });"></iframe>
		  |<noscript><a href="http://ad.doubleclick.net/N4215/jump/imdb2.consumer.title/maindetails;tile=1;sz=728x90,1008x150,1008x200,1008x30,970x250,1008x400,9x1;p=top;p=t;g=fm;g=co;g=an;g=ac;g=ad;g=baa;tt=f;m=PG;mh=PG;ml=PG;coo=us;fv=1;id=tt0441773;ab=c;bpx=2;md=tt0441773;s=3075;s=32;s=1009;s=3717;ord=065068742621?" target="_blank"><img src="http://ad.doubleclick.net/N4215/ad/imdb2.consumer.title/maindetails;tile=1;sz=728x90,1008x150,1008x200,1008x30,970x250,1008x400,9x1;p=top;p=t;g=fm;g=co;g=an;g=ac;g=ad;g=baa;tt=f;m=PG;mh=PG;ml=PG;coo=us;fv=1;id=tt0441773;ab=c;bpx=2;md=tt0441773;s=3075;s=32;s=1009;s=3717;ord=065068742621?" border="0" alt="advertisement" /></a></noscript>
		  |</div>
		  |<div id="top_ad_reflow_helper"></div>
		  |<script>
		  |doWithAds(function(){
		  |ad_utils.gpt.render_ad('top_ad');
		  |}, "ad_utils not defined, unable to render client-side GPT ad.");
		  |</script>
		  |<!-- End TOP_AD -->
		  |	
		  |</div>
		  |  <div id="navbar" class="navbarSprite">
		  |<noscript>
		  |  <link rel="stylesheet" type="text/css" href="http://ia.media-imdb.com/images/G/01/imdb/css/site/consumer-navbar-no-js-4175877511._V379390803_.css" />
		  |</noscript>
		  |<!--[if IE]><link rel="stylesheet" type="text/css" href="http://ia.media-imdb.com/images/G/01/imdb/css/site/consumer-navbar-ie-470687728._V379390980_.css"><![endif]-->
		  |<span id="home_img_holder">
		  |<a href="/?ref_=nv_home" title="Home" class="navbarSprite" id="home_img" ></a>  <span class="alt_logo">
		  |    <a href="/?ref_=nv_home" title="Home" >IMDb</a>
		  |  </span>
		  |</span>
		  |<form
		  |onsubmit="(new Image()).src='/rg/SEARCH-BOX/HEADER/images/b.gif?link=/find';"
		  | method="get"
		  | action="/find"
		  | class="nav-searchbar-inner"
		  | id="navbar-form"
		  |
		  |>
		  |  <div id="nb_search">
		  |    <noscript><div id="more_if_no_javascript"><a href="/search/">More</a></div></noscript>
		  |    <button id="navbar-submit-button" class="primary btn" type="submit"><div class="magnifyingglass navbarSprite"></div></button>
		  |    <input type="text" autocomplete="off" value="" name="q" id="navbar-query" placeholder="Find Movies, TV shows, Celebrities and more...">
		  |    <div class="quicksearch_dropdown_wrapper">
		  |      <select name="s" id="quicksearch" class="quicksearch_dropdown navbarSprite"
		  |              onchange="jumpMenu(this); suggestionsearch_dropdown_choice(this);">
		  |        <option value="all" >All</option>
		  |        <option value="tt" >Titles</option>
		  |        <option value="ep" >TV Episodes</option>
		  |        <option value="nm" >Names</option>
		  |        <option value="co" >Companies</option>
		  |        <option value="kw" >Keywords</option>
		  |        <option value="ch" >Characters</option>
		  |        <option value="vi" >Videos</option>
		  |        <option value="qu" >Quotes</option>
		  |        <option value="bi" >Bios</option>
		  |        <option value="pl" >Plots</option>
		  |      </select>
		  |    </div>
		  |    <div id="navbar-suggestionsearch"></div>
		  |  </div>
		  |</form>
		  |<div id="megaMenu">
		  |    <ul id="consumer_main_nav" class="main_nav">
		  |        <li class="spacer"></li>
		  |        <li class="css_nav_item" id="navTitleMenu">
		  |            <p class="navCategory">
		  |                <a href="/movies-in-theaters/?ref_=nv_tp_inth_1" >Movies</a>,
		  |                <a href="/tv/?ref_=nv_tp_tvhm_2" >TV</a><br />
		  |                & <a href="/showtimes/?ref_=nv_tp_sh_3" >Showtimes</a></p>
		  |            <span class="downArrow"></span>
		  |            <div id="navMenu1" class="sub_nav">
		  |                <div id="titleAd"></div>
		  |                <div class="subNavListContainer">
		  |                    <h5>MOVIES</h5>
		  |                    <ul>
		  |                        <li><a href="/movies-in-theaters/?ref_=nv_mv_inth_1" >In Theaters</a></li>
		  |                        <li><a href="/showtimes/?ref_=nv_mv_sh_2" >Showtimes & Tickets</a></li>
		  |                        <li><a href="/trailers/?ref_=nv_mv_tr_3" >Latest Trailers</a></li>
		  |                        <li><a href="/movies-coming-soon/?ref_=nv_mv_cs_4" >Coming Soon</a></li>
		  |                        <li><a href="/calendar/?ref_=nv_mv_cal_5" >Release Calendar</a></li>
		  |                    </ul>
		  |                    <h5>CHARTS & TRENDS</h5>
		  |                    <ul>
		  |                        <li><a href="/search/title?count=100&title_type=feature,tv_series&ref_=nv_ch_mm_1" >Popular Movies & TV</a></li>
		  |                        <li><a href="/chart/?ref_=nv_ch_cht_2" >Box Office</a></li>
		  |                        <li><a href="/search/title?count=100&groups=oscar_best_picture_winners&sort=year,desc&ref_=nv_ch_osc_3" >Oscar Winners</a></li>
		  |                        <li><a href="/chart/top?ref_=nv_ch_250_4" >Top 250</a></li>
		  |                        <li><a href="/genre/?ref_=nv_ch_gr_5" >Most Popular by Genre</a></li>
		  |                    </ul>
		  |                </div>
		  |                <div class="subNavListContainer">
		  |                    <h5>TV & VIDEO</h5>
		  |                    <ul>
		  |                        <li><a href="/tv/?ref_=nv_tvv_hm_1" >TV Home</a></li>
		  |                        <li><a href="/tvgrid/?ref_=nv_tvv_ls_2" >On Tonight</a></li>
		  |                        <li><a href="/watchnow/?ref_=nv_tvv_wn_3" >Watch Now on Amazon</a></li>
		  |                        <li><a href="/sections/dvd/?ref_=nv_tvv_dvd_4" >DVD & Blu-Ray</a></li>
		  |                        <li><a href="/tv/blog?ref_=nv_tvv_blog_5" >TV Blog</a></li>
		  |                    </ul>
		  |                    <h5>SPECIAL FEATURES</h5>
		  |                    <ul>
		  |                        <li><a href="/x-ray/?ref_=nv_sf_xray_1" >X-Ray for Movies & TV</a></li>
		  |                        <li><a href="/poll/?ref_=nv_sf_pl_2" >Polls</a></li>
		  |                    </ul>
		  |                </div>
		  |            </div>
		  |        </li>
		  |        <li class="spacer"></li>
		  |        <li class="css_nav_item" id="navNameMenu">
		  |            <p class="navCategory">
		  |                <a href="/search/name?gender=male,female&ref_=nv_tp_cel_1" >Celebs</a>,
		  |                <a href="/event/?ref_=nv_tp_ev_2" >Events</a><br />
		  |                & <a href="/media/index/rg1176148480?ref_=nv_tp_ph_3" >Photos</a></p>
		  |            <span class="downArrow"></span>
		  |            <div id="navMenu2" class="sub_nav">
		  |                <div id="nameAd"></div>
		  |                <div class="subNavListContainer">
		  |                    <h5>CELEBS</h5>
		  |                    <ul>
		  |                            <li><a href="/search/name?birth_monthday=05-21&refine=birth_monthday&ref_=nv_cel_brn_1" >Born Today</a></li>
		  |                        <li><a href="/news/celebrity?ref_=nv_cel_nw_2" >Celebrity News</a></li>
		  |                        <li><a href="/search/name?gender=male,female&ref_=nv_cel_m_3" >Most Popular Celebs</a></li>
		  |                    </ul>
		  |                    <h5>PHOTOS</h5>
		  |                    <ul>
		  |                        <li><a href="/media/index/rg1176148480?ref_=nv_ph_ls_1" >Latest Stills</a></li>
		  |                        <li><a href="/media/index/rg1528338944?ref_=nv_ph_lp_2" >Latest Posters</a></li>
		  |                        <li><a href="/sections/photos/premieres/?ref_=nv_ph_prem_3" >Movie & TV Premieres</a></li>
		  |                        <li><a href="/sections/photos/red_carpet/?ref_=nv_ph_red_4" >On the Red Carpet</a></li>
		  |                        <li><a href="/sections/photos/special_galleries/?ref_=nv_ph_sp_5" >Special Galleries</a></li>
		  |                    </ul>
		  |                </div>
		  |                <div class="subNavListContainer">
		  |                    <h5>EVENTS</h5>
		  |                    <ul>
		  |                        <li><a href="/sxsw/?ref_=nv_ev_sxsw_1" >SXSW Film Festival</a></li>
		  |                        <li><a href="/oscars/?ref_=nv_ev_rto_2" >Road to the Oscars</a></li>
		  |                        <li><a href="/emmys/?ref_=nv_ev_rte_3" >Road to the Emmys</a></li>
		  |                        <li><a href="/comic-con/?ref_=nv_ev_comic_4" >Comic-Con</a></li>
		  |                        <li><a href="/cannes/?ref_=nv_ev_can_5" >Cannes</a></li>
		  |                        <li><a href="/tribeca/?ref_=nv_ev_tri_6" >Tribeca</a></li>
		  |                        <li><a href="/sundance/?ref_=nv_ev_sun_7" >Sundance</a></li>
		  |                        <li><a href="/event/?ref_=nv_ev_all_8" >More Popular Events</a></li>
		  |                    </ul>
		  |                </div>
		  |            </div>
		  |        </li>
		  |        <li class="spacer"></li>
		  |        <li class="css_nav_item" id="navNewsMenu">
		  |            <p class="navCategory">
		  |                <a href="/news/top?ref_=nv_tp_nw_1" >News</a> &<br />
		  |                <a href="/boards/?ref_=nv_tp_bd_2" >Community</a></p>
		  |            <span class="downArrow"></span>
		  |            <div id="navMenu3" class="sub_nav">
		  |                <div id="latestHeadlines">
		  |                    <div class="subNavListContainer">
		  |                        <h5>LATEST HEADLINES</h5>
		  |    <ul>
		  |                <li itemprop="headline">
		  |<a href="/news/ni57204049/?ref_=nv_nw_tn_1" > Jimmy Kimmel Tells People ‘Godzilla’ Is Based on a True Story, Too Many Believe Him (Video)
		  |</a><br />
		  |                        <span class="time">20 May 2014</span>
		  |                </li>
		  |                <li itemprop="headline">
		  |<a href="/news/ni57202937/?ref_=nv_nw_tn_2" > 'Frozen' Set to Become Disney on Ice Show
		  |</a><br />
		  |                        <span class="time">20 May 2014</span>
		  |                </li>
		  |                <li itemprop="headline">
		  |<a href="/news/ni57204370/?ref_=nv_nw_tn_3" > Ryan Gosling Directorial Debut Leads to Critical Drubbing at Cannes
		  |</a><br />
		  |                        <span class="time">20 May 2014</span>
		  |                </li>
		  |    </ul>
		  |                    </div>
		  |                </div>
		  |                <div class="subNavListContainer">
		  |                    <h5>NEWS</h5>
		  |                    <ul>
		  |                        <li><a href="/news/top?ref_=nv_nw_tp_1" >Top News</a></li>
		  |                        <li><a href="/news/movie?ref_=nv_nw_mv_2" >Movie News</a></li>
		  |                        <li><a href="/news/tv?ref_=nv_nw_tv_3" >TV News</a></li>
		  |                        <li><a href="/news/celebrity?ref_=nv_nw_cel_4" >Celebrity News</a></li>
		  |                        <li><a href="/news/indie?ref_=nv_nw_ind_5" >Indie News</a></li>
		  |                    </ul>
		  |                    <h5>COMMUNITY</h5>
		  |                    <ul>
		  |                        <li><a href="/boards/?ref_=nv_cm_bd_1" >Message Boards</a></li>
		  |                        <li><a href="/czone/?ref_=nv_cm_cz_2" >Contributor Zone</a></li>
		  |                        <li><a href="/games/guess?ref_=nv_cm_qz_3" >Quiz Game</a></li>
		  |                        <li><a href="/poll/?ref_=nv_cm_pl_4" >Polls</a></li>
		  |                    </ul>
		  |                </div>
		  |            </div>
		  |        </li>
		  |        <li class="spacer"></li>
		  |        <li class="css_nav_item" id="navWatchlistMenu">
		  |<p class="navCategory singleLine watchlist">
		  |    <a href="/list/watchlist?ref_=nv_wl_all_0" >Watchlist</a>
		  |</p>
		  |<span class="downArrow"></span>
		  |<div id="navMenu4" class="sub_nav">
		  |    <h5>
		  |            YOUR WATCHLIST
		  |    </h5> 
		  |    <ul id="navWatchlist">
		  |    </ul>
		  |    <script>
		  |    if (!('imdb' in window)) { window.imdb = {}; }
		  |    window.imdb.watchlistTeaserData = [
		  |                {
		  |                        href : "/list/watchlist",
		  |                src : "http://ia.media-imdb.com/images/G/01/imdb/images/navbar/watchlist_slot1_logged_out-1670046337._V360061167_.jpg"
		  |                },
		  |                {
		  |                    href : "/search/title?count=100&title_type=feature,tv_series",
		  |                src : "http://ia.media-imdb.com/images/G/01/imdb/images/navbar/watchlist_slot2_popular-4090757197._V360060945_.jpg"
		  |                },
		  |                {
		  |                    href : "/chart/top",
		  |                src : "http://ia.media-imdb.com/images/G/01/imdb/images/navbar/watchlist_slot3_top250-575799966._V360061165_.jpg"
		  |                }
		  |    ];
		  |    </script>
		  |</div>
		  |        </li>
		  |        <li class="spacer"></li>
		  |    </ul>
		  |<script>
		  |if (!('imdb' in window)) { window.imdb = {}; }
		  |window.imdb.navbarAdSlots = {
		  |    titleAd : {
		  |            clickThru : "/title/tt0112573/",
		  |            imageUrl : "http://ia.media-imdb.com/images/M/MV5BMTAyMTgxMTg5NDVeQTJeQWpwZ15BbWU3MDQ3MDQ2ODM@._V1._SY315_CR0,0,410,315_CT10_.jpg",
		  |            titleYears : "1995",
		  |            rank : 79,
		  |                    headline : "Braveheart"
		  |    },
		  |    nameAd : {
		  |            clickThru : "/name/nm0005109/",
		  |            imageUrl : "http://ia.media-imdb.com/images/M/MV5BNDc5MTIwMzg1MF5BMl5BanBnXkFtZTcwMzg0MjQ5NQ@@._V1._SX270_CR15,0,250,315_.jpg",
		  |            rank : 58,
		  |            headline : "Mila Kunis"
		  |    }
		  |}
		  |</script>
		  |</div>
		  |<div id="nb_extra">
		  |    <ul id="nb_extra_nav" class="main_nav">
		  |        <li class="css_nav_item" id="navProMenu">
		  |            <p class="navCategory">
		  |<a href="http://pro.imdb.com/?ref_=cons_nb_hm" > <img alt="IMDbPro Menu" src="http://ia.media-imdb.com/images/G/01/imdb/images/navbar/imdbpro_logo_nb-720143162._V377744227_.png" />
		  |</a>            </p>
		  |            <span class="downArrow"></span>
		  |            <div id="navMenuPro" class="sub_nav">
		  |<a href="http://pro.imdb.com/?ref_=cons_nb_hm" id="proLink" > <div id="proAd">
		  |<script>
		  |if (!('imdb' in window)) { window.imdb = {}; }
		  |window.imdb.proMenuTeaser = {
		  |imageUrl : "http://ia.media-imdb.com/images/G/01/imdb/images/navbar/imdbpro_menu_user-2082544740._V377744226_.jpg"
		  |};
		  |</script>
		  |</div>
		  |<div class="subNavListContainer">
		  |<img alt="Go to IMDbPro" title="Go to IMDbPro" src="http://ia.media-imdb.com/images/G/01/imdb/images/navbar/imdbpro_logo_menu-2185879182._V377744253_.png" />
		  |<h5>GET INFORMED</h5>
		  |<p>Industry information at your fingertips</p>
		  |<h5>GET CONNECTED</h5>
		  |<p>Over 200,000 Hollywood insiders</p>
		  |<h5>GET DISCOVERED</h5>
		  |<p>Enhance your IMDb Page</p>
		  |<p><strong>Go to IMDbPro &raquo;</strong></p>
		  |</div>
		  |</a>            </div>
		  |        </li>
		  |        <li class="spacer"><span class="ghost">|</span></li>
		  |        <li>
		  |            <a href="/apps/?ref_=nb_app" >IMDb Apps</a>
		  |        </li>
		  |        <li class="spacer"><span class="ghost">|</span></li>
		  |        <li>
		  |            <a href="/help/?ref_=nb_hlp" >Help</a>
		  |        </li>
		  |    </ul>
		  |</div>
		  |<div id="nb_personal">
		  |    <ul id="consumer_user_nav" class="main_nav">
		  |        <li class="css_nav_menu" id="navUserMenu">
		  |            <p class="navCategory singleLine">        
		  |                <a rel="login" href="/register/login?ref_=nv_usr_lgin_1" id="nblogin" >Login</a>
		  |            </p>
		  |            <span class="downArrow"></span>
		  |            <div class="sub_nav">
		  |                <div class="subNavListContainer">
		  |                    <br />
		  |                    <ul>
		  |                        <li>
		  |                            <a href="https://secure.imdb.com/register-imdb/form-v2?ref_=nv_usr_reg_2" >Register</a>
		  |                        </li>
		  |                        <li>
		  |                            <a rel="login" href="/register/login?ref_=nv_usr_lgin_3" id="nblogin" >Login</a>
		  |                        </li>
		  |                    </ul>
		  |                </div>
		  |            </div>
		  |        </li>
		  |    </ul>
		  |</div>
		  |  </div>
		  |</div>
		  |	
		  |	<!-- no content received for slot: navstrip -->
		  |	
		  |	
		  |	<!-- begin injectable INJECTED_NAVSTRIP -->
		  |<div id="injected_navstrip_wrapper" class="injected_slot">
		  |<iframe id="injected_navstrip" name="injected_navstrip" class="yesScript" width="0" height="0" data-dart-params="#imdb2.consumer.title/maindetails;oe=utf-8;u=065068742621;ord=065068742621?" data-original-width="0" data-original-height="0" data-config-width="0" data-config-height="0" data-cookie-width="null" data-cookie-height="null" marginwidth="0" marginheight="0" frameborder="0" scrolling="no" allowtransparency="true" onload="doWithAds.call(this, function(){ ad_utils.on_ad_load(this); });"></iframe> </div>
		  |<script>
		  |doWithAds(function(){
		  |ad_utils.inject_ad.register('injected_navstrip');
		  |}, "ad_utils not defined, unable to render injected ad.");
		  |</script>
		  |<div id="injected_navstrip_reflow_helper"></div>
		  |<!-- end injectable INJECTED_NAVSTRIP -->
		  |	
		  |<script>
		  |    if (typeof uet == 'function') {
		  |      uet("ne");
		  |    }
		  |</script>
		  |                    <div id="pagecontent" itemscope itemtype="http://schema.org/Movie">
		  |	
		  |	<!-- begin injectable INJECTED_BILLBOARD -->
		  |<div id="injected_billboard_wrapper" class="injected_slot">
		  |<iframe id="injected_billboard" name="injected_billboard" class="yesScript" width="0" height="0" data-dart-params="#imdb2.consumer.title/maindetails;oe=utf-8;u=065068742621;ord=065068742621?" data-original-width="0" data-original-height="0" data-config-width="0" data-config-height="0" data-cookie-width="null" data-cookie-height="null" marginwidth="0" marginheight="0" frameborder="0" scrolling="no" allowtransparency="true" onload="doWithAds.call(this, function(){ ad_utils.on_ad_load(this); });"></iframe> </div>
		  |<script>
		  |doWithAds(function(){
		  |ad_utils.inject_ad.register('injected_billboard');
		  |}, "ad_utils not defined, unable to render injected ad.");
		  |</script>
		  |<div id="injected_billboard_reflow_helper"></div>
		  |<!-- end injectable INJECTED_BILLBOARD -->
		  |	
		  |
		  |	
		  |	<!-- begin NAVBOARD -->
		  |<div id="navboard_wrapper" class="cornerstone_slot">
		  |<script type="text/javascript">
		  |doWithAds(function(){
		  |ad_utils.register_ad('navboard');
		  |});
		  |</script>
		  |<iframe id="navboard" name="navboard" class="yesScript" width="1008" height="377" data-original-width="1008" data-original-height="377" data-blank-serverside marginwidth="0" marginheight="0" frameborder="0" scrolling="no" allowtransparency="true" onload="doWithAds.call(this, function(){ ad_utils.on_ad_load(this); });" allowfullscreen="true"></iframe>
		  |</div>
		  |<div id="navboard_reflow_helper"></div>
		  |<script>
		  |doWithAds(function(){
		  |ad_utils.inject_serverside_ad('navboard', '');
		  |},"unable to inject serverside ad");
		  |</script>
		  |	
		  |
		  |<div id="content-2-wide" class="redesign">
		  |    <div class="maindetails_center" id="maindetails_center_top">
		  |
		  |           
		  |            <div class="article title-overview">
		  |
		  |                
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_TitleOverviewWidget_started');
		  |    }
		  |  </script>
		  |    <div id="title-overview-widget">
		  |        <table cellspacing="0" cellpadding="0" border="0" id="title-overview-widget-layout">
		  |            <tbody>
		  |                <tr>
		  |                    <td rowspan="2" id="img_primary">
		  |                            <div class="image">
		  |<a href="/media/rm2261620224/tt0441773?ref_=tt_ov_i" > <img height="317"
		  |width="214"
		  |alt="Kung Fu Panda (2008) Poster"
		  |title="Kung Fu Panda (2008) Poster"
		  |src="http://ia.media-imdb.com/images/M/MV5BMTIxOTY1NjUyN15BMl5BanBnXkFtZTcwMjMxMDk1MQ@@._V1_SX214_AL_.jpg"
		  |itemprop="image" />
		  |</a>                            </div> 
		  |    
		  |        <div class="pro-title-link text-center">
		  |<a href="http://pro.imdb.com/title/tt0441773?ref_=cons_tt_contact" >Contact the Filmmakers on IMDbPro &raquo;</a>
		  |        </div>
		  |                    </td>
		  |                    <td id="overview-top">
		  |    
		  |    <div id="prometer_container">
		  |        <div id="prometer" class="meter-collapsed down">
		  |            <div id="meterHeaderBox">
		  |                <div id="meterTitle" class="meterToggleOnHover">MOVIEmeter</div>
		  |<a href="http://pro.imdb.com/title/tt0441773?ref_=cons_tt_meter" id="meterRank" >Top 5000
		  |</a>            </div>
		  |            <div id="meterChangeRow" class="meterToggleOnHover">
		  |                    <span>Down</span>
		  |                <span id="meterChange">10</span>
		  |                <span>this week</span>
		  |            </div>
		  |            <div id="meterSeeMoreRow" class="meterToggleOnHover">
		  |<a href="http://pro.imdb.com/title/tt0441773?ref_=cons_tt_meter" >View rank on IMDbPro</a>
		  |                <span>&raquo;</span>
		  |            </div>
		  |        </div>
		  |    </div>
		  |<h1 class="header"> <span class="itemprop" itemprop="name">Kung Fu Panda</span> 
		  |            <span class="nobr">(<a href="/year/2008/?ref_=tt_ov_inf" >2008</a>)</span>
		  |    
		  |</h1>
		  |    <div class="infobar">
		  |
		  |            
		  |                <span title="PG"
		  |                          class="us_pg titlePageSprite absmiddle"
		  |                          itemprop="contentRating" content="PG"></span>
		  |        
		  |        
		  |        
		  |        
		  |            <time itemprop="duration" datetime="PT90M" >
		  |                90 min
		  |</time>        
		  |                &nbsp;-&nbsp;
		  |<a href="/genre/Animation?ref_=tt_ov_inf" ><span class="itemprop" itemprop="genre">Animation</span></a>
		  | <span class="ghost">|</span> 
		  |<a href="/genre/Action?ref_=tt_ov_inf" ><span class="itemprop" itemprop="genre">Action</span></a>
		  | <span class="ghost">|</span> 
		  |<a href="/genre/Adventure?ref_=tt_ov_inf" ><span class="itemprop" itemprop="genre">Adventure</span></a>
		  |                &nbsp;-&nbsp;
		  |            <span class="nobr">
		  |<a href="/title/tt0441773/releaseinfo?ref_=tt_ov_inf " title="See all release dates" > 6 June 2008<meta itemprop="datePublished" content="2008-06-06" />
		  |(USA)
		  |</a>            </span>
		  |        
		  |    </div>
		  |
		  |<div class="star-box giga-star">
		  |        <div class="titlePageSprite star-box-giga-star"> 7.6 </div>
		  |    <div class="star-box-rating-widget">
		  |            <span class="star-box-rating-label">Your rating:</span>
		  |<div class="rating rating-list" data-starbar-class="rating-list" data-auth="" data-user="" id="tt0441773|imdb|0|0|title-maindetails|tt0441773|title|main" data-ga-identifier=""
		  |title="Users rated this 7.6/10 (213,317 votes) - click stars to rate" >
		  |<span class="rating-bg">&nbsp;</span>
		  |<span class="rating-imdb " style="width: 0px">&nbsp;</span>
		  |<span class="rating-stars">
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>1</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>2</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>3</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>4</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>5</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>6</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>7</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>8</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>9</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>10</span></a>
		  |</span>
		  |<span class="rating-rating "><span class="value">-</span><span class="grey">/</span><span class="grey">10</span></span>
		  |<span class="rating-cancel "><a href="/title/tt0441773/vote?v=X;k=" title="Delete" rel="nofollow"><span>X</span></a></span>
		  |&nbsp;</div>
		  |    </div> 
		  |    <div class="star-box-details" itemtype="http://schema.org/AggregateRating" itemscope itemprop="aggregateRating">
		  |            Ratings:
		  |<strong><span itemprop="ratingValue">7.6</span></strong><span class="mellow">/<span itemprop="bestRating">10</span></span>            from <a href="ratings?ref_=tt_ov_rt" title="213,317 IMDb users have given a weighted average vote of 7.6/10" > <span itemprop="ratingCount">213,317</span> users
		  |</a>&nbsp;
		  |            Metascore: <a href="criticreviews?ref_=tt_ov_rt" title="73 review excerpts provided by Metacritic.com" > 73/100
		  |</a>            <br/>
		  |            Reviews: 
		  |<a href="reviews?ref_=tt_ov_rt" title="336 IMDb user reviews" > <span itemprop="reviewCount">336 user</span>
		  |</a> 
		  |                <span class="ghost">|</span>
		  |<a href="externalreviews?ref_=tt_ov_rt" title="254 IMDb critic reviews" > <span itemprop="reviewCount">254 critic</span>
		  |</a>             
		  |                <span class="ghost">|</span> 
		  |<a href="criticreviews?ref_=tt_ov_rt" title="33 review excerpts provided by Metacritic.com" > 33
		  |</a>                from
		  |<a href="http://www.metacritic.com" target='_blank'> Metacritic.com
		  |</a>             
		  |         
		  |    </div> 
		  |    <div class="clear"></div>
		  |</div> 
		  |                        <p></p>
		  |<p itemprop="description">
		  |In the Valley of Peace, Po the Panda finds himself chosen as the Dragon Warrior despite the fact that he is obese and a complete novice at martial arts.</p>
		  |                        <p></p>
		  |    <div class="txt-block" itemprop="director" itemscope itemtype="http://schema.org/Person">
		  |        <h4 class="inline">Directors:</h4>
		  |<a href="/name/nm0651706/?ref_=tt_ov_dr" itemprop='url'><span class="itemprop" itemprop="name">Mark Osborne</span></a>, 
		  |<a href="/name/nm0828970/?ref_=tt_ov_dr" itemprop='url'><span class="itemprop" itemprop="name">John Stevenson</span></a>
		  |    </div>
		  |    <div class="txt-block" itemprop="creator" itemscope itemtype="http://schema.org/Person"> 
		  |        <h4 class="inline">Writers:</h4>
		  |<a href="/name/nm0008743/?ref_=tt_ov_wr" itemprop='url'><span class="itemprop" itemprop="name">Jonathan Aibel</span></a>               (screenplay), 
		  |<a href="/name/nm0074184/?ref_=tt_ov_wr" itemprop='url'><span class="itemprop" itemprop="name">Glenn Berger</span></a>               (screenplay), <a href="fullcredits?ref_=tt_ov_wr#writers" >2 more credits</a>&nbsp;&raquo;
		  |    </div> 
		  |                         
		  |                          <div class="txt-block" itemprop="actors" itemscope itemtype="http://schema.org/Person"> 
		  |                            <h4 class="inline">Stars:</h4>
		  |<a href="/name/nm0085312/?ref_=tt_ov_st" itemprop='url'><span class="itemprop" itemprop="name">Jack Black</span></a>, 
		  |<a href="/name/nm0574534/?ref_=tt_ov_st" itemprop='url'><span class="itemprop" itemprop="name">Ian McShane</span></a>, 
		  |<a href="/name/nm0001401/?ref_=tt_ov_st" itemprop='url'><span class="itemprop" itemprop="name">Angelina Jolie</span></a>                          <span class="ghost">|</span>
		  |                          <span class="see-more inline nobr">
		  |<a href="fullcredits?ref_=tt_ov_st_sm" itemprop='url'> See full cast and crew</a> &raquo;
		  |                          </span>
		  |                        </div> 
		  |                       
		  |                     
		  |                   </td>
		  |                </tr>
		  |                <tr>
		  |                    <td id="overview-bottom">
		  |                        <div class="wlb_classic_wrapper">
		  |                          <span class="wlb_wrapper">
		  |                            <a class="wlb_watchlist_btn" data-tconst="tt0441773" data-size="large" data-caller-name="title" data-type="primary">
		  |                            </a>
		  |                            <a class="wlb_dropdown_btn" data-tconst="tt0441773" data-size="large" data-caller-name="title" data-type="primary">
		  |                            </a>
		  |                          </span>
		  |                          <div class="wlb_dropdown_list" style="display:none;">
		  |                          </div>
		  |                          <div class="wlb_alert" style="display:none">
		  |                          </div>
		  |                        </div>
		  |    
		  |        
		  |<a href="/video/imdb/vi330432793/?ref_=tt_ov_vi" class="btn2 btn2_text_on large title-trailer video-colorbox" data-type="recommends" data-tconst="tt0441773" data-video="vi330432793" data-context="imdb" itemprop="trailer"> <span class="btn2_text">Watch Trailer</span>
		  |</a>
		  |<div id="share-checkin">
		  |<div class="add_to_checkins" data-const="tt0441773" data-lcn="title-maindetails">
		  |<span class="btn2_wrapper"><a onclick='' class="btn2 large btn2_text_on disabled checkins_action_btn"><span class="btn2_glyph">0</span><span class="btn2_text">Check in</span></a></span>    <div class="popup checkin-dialog">
		  |        <a class="small disabled close btn">X</a>
		  |        <span class="beta">Beta</span>
		  |        <span class="title">I'm Watching This!</span>
		  |        <div class="body">
		  |            <div class="info">Keep track of everything you watch; tell your friends.
		  |            	<div class="info_details">
		  |            		If your account is linked with Facebook and you have turned on sharing, this will show up in your activity feed. If not, you can turn on sharing 
		  |            			<a
		  |onclick="(new Image()).src='/rg/unknown/unknown/images/b.gif?link=https://secure.imdb.com/register-imdb/sharing?ref=tt_checkin_share';"
		  |href="https://secure.imdb.com/register-imdb/sharing?ref=tt_checkin_share"
		  |>here</a>
		  |            		.
		  |            	</div>
		  |            </div>
		  |            <div class="small message_box">
		  |                <div class="hidden error"><h2>Error</h2> Please try again!</div>
		  |                <div class="hidden success"><h2>Added to Your Check-Ins.</h2> <a href="/list/checkins">View</a></div>
		  |            </div>
		  |            <textarea data-msg="Enter a comment..."></textarea>
		  |            <div class="share">
		  |                <button class="large primary btn"><span>Check in</span></button>
		  |<!--
		  |                    Check-ins are more fun when<br>
		  |                    you <a href="/register/sharing">enable Facebook sharing</a>!
		  |-->
		  |            </div>
		  |        </div>
		  |    </div>
		  |    <input type="hidden" name="49e6c" value="e341">
		  |</div>
		  |</div>
		  |<span class="btn2_wrapper"><a onclick='' class="btn2 large btn2_text_on launch-share-popover"><span class="btn2_glyph">0</span><span class="btn2_text">Share...</span></a></span><div id="share-popover">
		  |    <a class="close-popover" href="#">X</a>
		  |    <h4>Share</h4>
		  |
		  |     
		  |    
		  |    <a onclick="window.open(&quot;http://www.facebook.com/sharer.php?u=http%3A%2F%2Fwww.imdb.com%2Frg%2Fs%2F3%2Ftitle%2Ftt0441773%3Fref_%3Dext_shr_fb_tt&quot;, 'newWindow', 'width=626,height=436'); return false;"
		  |       href="http://www.facebook.com/sharer.php?u=http%3A%2F%2Fwww.imdb.com%2Frg%2Fs%2F3%2Ftitle%2Ftt0441773%3Fref_%3Dext_shr_fb_tt"
		  |       title="Share on Facebook"
		  |       class="facebook"
		  |       ref_="tt_shr_fb"
		  |       target="_blank"><div class="option facebook">
		  |                            <span class="titlePageSprite share_facebook"></span>
		  |                            <div>Facebook</div>
		  |                        </div></a>
		  |
		  |    
		  |    
		  |    <a onclick="window.open(&quot;http://twitter.com/intent/tweet?text=Kung%20Fu%20Panda%20(2008)%20-%20imdb.com%2Frg%2Fs%2F1%2Ftitle%2Ftt0441773%3Fref_%3Dext_shr_tw_tt&quot;, 'newWindow', 'width=815,height=436'); return false;"
		  |       href="http://twitter.com/intent/tweet?text=Kung%20Fu%20Panda%20(2008)%20-%20imdb.com%2Frg%2Fs%2F1%2Ftitle%2Ftt0441773%3Fref_%3Dext_shr_tw_tt"
		  |       title="Share on Twitter"
		  |       class="twitter"
		  |       ref_="tt_shr_tw"
		  |       target="_blank"><div class="option twitter">
		  |                            <span class="titlePageSprite share_twitter"></span>
		  |                            <div>Twitter</div>
		  |                        </div></a>
		  |    
		  |    <a href="mailto:?subject=IMDb%3A%20Kung%20Fu%20Panda%20(2008)&body=IMDb%3A%20Kung%20Fu%20Panda%20(2008)%0AIn%20the%20Valley%20of%20Peace%2C%20Po%20the%20Panda%20finds%20himself%20chosen%20as%20the%20Dragon%20Warrior%20despite%20the%20fact%20that%20he%20is%20obese%20and%20a%20complete%20novice%20at%20martial%20arts.%0Ahttp%3A%2F%2Fwww.imdb.com%2Frg%2Fem_share%2Ftitle_web%2Ftitle%2Ftt0441773%3Fref%3Dext_shr_eml_tt" 
		  |       title="Share by e-mail"
		  |       class=""
		  |       ref="tt_shr_eml"><div class='option email'>
		  |                        <span class='titlePageSprite share_email'></span>
		  |                        <div>E-mail</div>
		  |                    </div></a>
		  |
		  |        <a href="#" class="open-checkin-popover">
		  |            <div class="option checkin">
		  |                <span class="titlePageSprite share_checkin"></span>
		  |                <div>Check in</div>
		  |            </div>
		  |        </a>
		  |</div>
		  |
		  |                    </td>
		  |                </tr>
		  |            </tbody>
		  |        </table>
		  |    </div>
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_TitleOverviewWidget_finished');
		  |    }
		  |  </script>
		  |
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_WatchBarWidget_started');
		  |    }
		  |  </script>
		  |        <div class="watch-bar">
		  |
		  |        
		  |<a rel="nofollow" href="/offsite/?page-action=watch-aiv&token=BCYuwQ-ojJ6j6CzYgB795ufFgNtGKEGa_NrsZvII-jvOwlNkY5TyBFg57dDWhHB99t1E4lkl0EwE%0D%0AuIyl5D-SqfR2alMPWxAKokZQyfX8fRmZCARmDTtw7HA8hX2pvCVNjufT4Up1Y6s2_TWEqea_uX4Z%0D%0ANPTFEHVSNifTi4dvQnafzSAwPfYKZoFWXuy0yk1BX-pRwcfB8WOfEi0LYBOuEXmf7je2NOfWM9jc%0D%0Al6O8LIgXlwQbbkYDGNlm45kZY8LEwbbBbxJv-7UfDfAfJV3Z8Shcxw%0D%0A&ref_=tt_wb_amazon" target="_target"> <div class="watch-lozenge">
		  |<span class="titlePageSprite aiv38"></span>
		  |<h3>Watch now</h3>
		  |<p>At Amazon Instant Video</p>
		  |</div>
		  |</a>                <div class="watch-divider">&nbsp;</div>
		  |
		  |        
		  |<a rel="nofollow" href="/rj/?p=amazon&m=dvd&s=DE&c=tt0441773&t=imdb-adbox-de-21&ref_=tt_wb_amzn" target="_target"> <div class="watch-lozenge">
		  |<span class="titlePageSprite buyam38"></span>
		  |<h3>Own it</h3>
		  |<p>Buy it at Amazon.de</p>
		  |</div>
		  |</a>                
		  |        </div>
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_WatchBarWidget_finished');
		  |    }
		  |  </script>
		  |                
		  |            </div>             
		  |        
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_atf_main');
		  |    }
		  |  </script>
		  |    </div> 
		  |    
		  |<script>
		  |    if (typeof uet == 'function') {
		  |      uet("cf");
		  |    }
		  |</script>
		  |    
		  |    <div id="maindetails_sidebar_top" class="maindetails_sidebar">
		  |	
		  |	<!-- begin TOP_RHS -->
		  |<div id="top_rhs_wrapper" class="dfp_slot">
		  |<script type="text/javascript">
		  |doWithAds(function(){
		  |ad_utils.register_ad('top_rhs');
		  |});
		  |</script>
		  |<iframe data-dart-params="#imdb2.consumer.title/maindetails;!TILE!;sz=300x250,300x600,300x300,11x1;p=tr;g=fm;g=co;g=an;g=ac;g=ad;g=baa;tt=f;m=PG;mh=PG;ml=PG;coo=us;fv=1;id=tt0441773;ab=c;bpx=2;md=tt0441773;s=3075;s=32;s=1009;s=3717;oe=utf-8;[CLIENT_SIDE_KEYVALUES];u=065068742621;ord=065068742621?" id="top_rhs" name="top_rhs" class="yesScript" width="300" height="250" data-original-width="300" data-original-height="250" data-config-width="300" data-config-height="250" data-cookie-width="null" data-cookie-height="null" marginwidth="0" marginheight="0" frameborder="0" scrolling="no" allowtransparency="true" onload="doWithAds.call(this, function(){ ad_utils.on_ad_load(this); });"></iframe>
		  |<noscript><a href="http://ad.doubleclick.net/N4215/jump/imdb2.consumer.title/maindetails;tile=0;sz=300x250,300x600,300x300,11x1;p=tr;g=fm;g=co;g=an;g=ac;g=ad;g=baa;tt=f;m=PG;mh=PG;ml=PG;coo=us;fv=1;id=tt0441773;ab=c;bpx=2;md=tt0441773;s=3075;s=32;s=1009;s=3717;ord=065068742621?" target="_blank"><img src="http://ad.doubleclick.net/N4215/ad/imdb2.consumer.title/maindetails;tile=0;sz=300x250,300x600,300x300,11x1;p=tr;g=fm;g=co;g=an;g=ac;g=ad;g=baa;tt=f;m=PG;mh=PG;ml=PG;coo=us;fv=1;id=tt0441773;ab=c;bpx=2;md=tt0441773;s=3075;s=32;s=1009;s=3717;ord=065068742621?" border="0" alt="advertisement" /></a></noscript>
		  |</div>
		  |<div id="top_rhs_reflow_helper"></div>
		  |<div id="top_rhs_after" class="after_ad" style="visibility:hidden;">
		  |<a class="yesScript" href="#" onclick="ad_utils.show_ad_feedback('top_rhs');return false;" id="ad_feedback_top_rhs">ad feedback</a>
		  |</div>
		  |<script>
		  |doWithAds(function(){
		  |ad_utils.gpt.render_ad('top_rhs');
		  |}, "ad_utils not defined, unable to render client-side GPT ad.");
		  |</script>
		  |<!-- End TOP_RHS -->
		  |	
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_atf_sidebar');
		  |    }
		  |  </script>
		  |    </div> 
		  |
		  |<script>
		  |    if (typeof uet == 'function') {
		  |      uet("af");
		  |    }
		  |</script>
		  |
		  |    <div id="maindetails_sidebar_bottom" class="maindetails_sidebar">
		  |        
		  |        
		  |            
		  |    
		  |    
		  |    <div class="aux-content-widget-3 links subnav" div="quicklinks">
		  |
		  |            <h3>Quick Links</h3>
		  |
		  |
		  |        <div id="maindetails_quicklinks">
		  |                <div class="split_0">    <ul class="quicklinks">
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/fullcredits?ref_=tt_ql_1" class="link" >Full Cast and Crew</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/trivia?ref_=tt_ql_2" class="link" >Trivia</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/quotes?ref_=tt_ql_3" class="link" >Quotes</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/awards?ref_=tt_ql_4" class="link" >Awards</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/board/?ref_=tt_ql_5" class="link" >Message Board</a>
		  |                </li>
		  |    </ul>
		  |</div>
		  |                <div class="split_1">    <ul class="quicklinks">
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/plotsummary?ref_=tt_ql_6" class="link" >Plot Summary</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/parentalguide?ref_=tt_ql_7" class="link" >Parents Guide</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/reviews?ref_=tt_ql_8" class="link" >User Reviews</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/releaseinfo?ref_=tt_ql_9" class="link" >Release Dates</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/companycredits?ref_=tt_ql_10" class="link" >Company Credits</a>
		  |                </li>
		  |    </ul>
		  |</div>
		  |        </div>
		  |        <hr/>
		  |
		  |        <div id="full_subnav">
		  |
		  |
		  |        <h4>Details</h4>
		  |    <ul class="quicklinks">
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/fullcredits?ref_=tt_ql_dt_1" class="link" >Full Cast and Crew</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/releaseinfo?ref_=tt_ql_dt_2" class="link" >Release Dates</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/officialsites?ref_=tt_ql_dt_3" class="link" >Official Sites</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/business?ref_=tt_ql_dt_4" class="link" >Box Office/Business</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/companycredits?ref_=tt_ql_dt_5" class="link" >Company Credits</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/locations?ref_=tt_ql_dt_6" class="link ghost" >Filming Locations</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/technical?ref_=tt_ql_dt_7" class="link" >Technical Specs</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/literature?ref_=tt_ql_dt_8" class="link ghost" >Literature</a>
		  |                </li>
		  |    </ul>
		  |
		  |
		  |        <h4>Storyline</h4>
		  |    <ul class="quicklinks">
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/taglines?ref_=tt_ql_stry_1" class="link" >Taglines</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/plotsummary?ref_=tt_ql_stry_2" class="link" >Plot Summary</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/synopsis?ref_=tt_ql_stry_3" class="link" >Synopsis</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/keywords?ref_=tt_ql_stry_4" class="link" >Plot Keywords</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/parentalguide?ref_=tt_ql_stry_5" class="link" >Parents Guide</a>
		  |                </li>
		  |    </ul>
		  |
		  |
		  |        <h4>Did You Know?</h4>
		  |    <ul class="quicklinks">
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/trivia?ref_=tt_ql_trv_1" class="link" >Trivia</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/goofs?ref_=tt_ql_trv_2" class="link" >Goofs</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/crazycredits?ref_=tt_ql_trv_3" class="link" >Crazy Credits</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/quotes?ref_=tt_ql_trv_4" class="link" >Quotes</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/alternateversions?ref_=tt_ql_trv_5" class="link" >Alternate Versions</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/movieconnections?ref_=tt_ql_trv_6" class="link" >Connections</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/soundtrack?ref_=tt_ql_trv_7" class="link" >Soundtracks</a>
		  |                </li>
		  |    </ul>
		  |
		  |
		  |        <h4>Photo & Video</h4>
		  |    <ul class="quicklinks">
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/mediaindex?ref_=tt_ql_pv_1" class="link" >Photo Gallery</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/videogallery?ref_=tt_ql_pv_2" class="link" >Trailers and Videos</a>
		  |                </li>
		  |    </ul>
		  |
		  |
		  |        <h4>Opinion</h4>
		  |    <ul class="quicklinks">
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/awards?ref_=tt_ql_op_1" class="link" >Awards</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/faq?ref_=tt_ql_op_2" class="link" >FAQ</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/reviews?ref_=tt_ql_op_3" class="link" >User Reviews</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/ratings?ref_=tt_ql_op_4" class="link" >User Ratings</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/externalreviews?ref_=tt_ql_op_5" class="link" >External Reviews</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/criticreviews?ref_=tt_ql_op_6" class="link" >Metacritic Reviews</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/board/?ref_=tt_ql_op_7" class="link" >Message Board</a>
		  |                </li>
		  |    </ul>
		  |
		  |
		  |        <h4>TV</h4>
		  |    <ul class="quicklinks">
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/tvschedule?ref_=tt_ql_tv_1" class="link ghost" >TV Schedule</a>
		  |                </li>
		  |    </ul>
		  |
		  |
		  |        <h4>Related Items</h4>
		  |    <ul class="quicklinks">
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/news?ref_=tt_ql_rel_1" class="link" >NewsDesk</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/showtimes/title/tt0441773?ref_=tt_ql_rel_2" class="link ghost" >Showtimes</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="/title/tt0441773/externalsites?ref_=tt_ql_rel_3" class="link" >External Sites</a>
		  |                </li>
		  |    </ul>
		  |
		  |
		  |        <h4>Professional Services</h4>
		  |    <ul class="quicklinks">
		  |                <li class="subnav_item_main">
		  |<a href="http://pro.imdb.com/title/tt0441773?ref_=tt_ql_pro_1" class="link" >Get more at IMDbPro</a>
		  |                </li>
		  |                <li class="subnav_item_main">
		  |<a href="https://secure.imdb.com/store/photos/?ref_=tt_ql_pro_2" class="link" >Add posters & stills to this title</a>
		  |                </li>
		  |    </ul>
		  |            <hr/>
		  |        </div>
		  |
		  |        <div class="show_more"><span class="titlePageSprite arrows show"></span>Explore More</div>
		  |        <div class="show_less"><span class="titlePageSprite arrows hide"></span>Show Less</div>
		  |    </div>
		  |
		  |
		  |  <div class="aux-content-widget-2"> 
		  |    <div class="social">     
		  |  <script type="text/javascript">generic.monitoring.start_timing("facebook_like_iframe");</script>
		  |  <div class="social_networking_like">
		  |    <iframe
		  |      id="iframe_like"
		  |      name="fbLikeIFrame_0"
		  |      class="social-iframe"
		  |      scrolling="no"
		  |      frameborder="0"
		  |      allowTransparency="allowTransparency"
		  |      ref="http://www.imdb.com/title/tt0441773/"
		  |      width=280
		  |      height=65></iframe>
		  |  </div>
		  |    </div>
		  |  </div>
		  |	
		  |	<!-- begin RHS_CORNERSTONE -->
		  |<div id="rhs_cornerstone_wrapper" class="cornerstone_slot">
		  |<script type="text/javascript">
		  |doWithAds(function(){
		  |ad_utils.register_ad('rhs_cornerstone');
		  |});
		  |</script>
		  |<iframe id="rhs_cornerstone" name="rhs_cornerstone" class="yesScript" width="300" height="125" data-original-width="300" data-original-height="125" data-blank-serverside marginwidth="0" marginheight="0" frameborder="0" scrolling="no" allowtransparency="true" onload="doWithAds.call(this, function(){ ad_utils.on_ad_load(this); });" allowfullscreen="true"></iframe>
		  |</div>
		  |<div id="rhs_cornerstone_reflow_helper"></div>
		  |<script>
		  |doWithAds(function(){
		  |ad_utils.inject_serverside_ad('rhs_cornerstone', '');
		  |},"unable to inject serverside ad");
		  |</script>
		  |	
		  |
		  |
		  |
		  |      <div class="aux-content-widget-2" >
		  |        <h3>Related News</h3>
		  |
		  |        
		  |                
		  |                    
		  |                    
		  |                    <div class="news_item odd" >
		  |                        <span itemprop="headline" >
		  |                            <a href="/title/tt0441773/news?ref_=tt_nwr_1#ni57206042" >'The Penguins of Madagascar' Moves to Thanksgiving 2014</a>
		  |                        </span>
		  |                        
		  |                        <br /><small>
		  |                                <span itemprop="datePublished">
		  |                                  15 hours ago
		  |                                </span>
		  |                            
		  |                                <span class="ghost">|</span><span itemprop="provider" >
		  |                                        <a href="/news/ns0000254?ref_=tt_nwr_1" >MovieWeb</a>
		  |                                </span>
		  |                        </small>
		  |                    </div>
		  |                
		  |                    
		  |                    
		  |                    <div class="news_item even" >
		  |                        <span itemprop="headline" >
		  |                            <a href="/title/tt0441773/news?ref_=tt_nwr_2#ni57204328" >Entertainment One Nabs ‘Little Prince,’ ‘Somnia,’ ‘Viral’ in Canada</a>
		  |                        </span>
		  |                        
		  |                        <br /><small>
		  |                                <span itemprop="datePublished">
		  |                                  20 May 2014
		  |                                </span>
		  |                            
		  |                                <span class="ghost">|</span><span itemprop="provider" >
		  |                                        <a href="/news/ns0052791?ref_=tt_nwr_2" >Variety - Film News</a>
		  |                                </span>
		  |                        </small>
		  |                    </div>
		  |                
		  |                    
		  |                    
		  |                    <div class="news_item odd" >
		  |                        <span itemprop="headline" >
		  |                            <a href="/title/tt0441773/news?ref_=tt_nwr_3#ni57193519" >Cannes: ‘How to Train Your Dragon 2′ Director Dean DeBlois Digs Deeper</a>
		  |                        </span>
		  |                        
		  |                        <br /><small>
		  |                                <span itemprop="datePublished">
		  |                                  16 May 2014
		  |                                </span>
		  |                            
		  |                                <span class="ghost">|</span><span itemprop="provider" >
		  |                                        <a href="/news/ns0052791?ref_=tt_nwr_3" >Variety - Film News</a>
		  |                                </span>
		  |                        </small>
		  |                    </div>
		  |       
		  |            <div class="see-more">
		  |                <a href="/title/tt0441773/news?ref_=tt_nwr_sm" >See all 2305 related articles</a>&nbsp;&raquo;
		  |            </div>
		  |
		  |      </div>
		  |
		  |        
		  |	
		  |	<!-- no content received for slot: middle_rhs -->
		  |	
		  |
		  |        
		  |        
		  |        <div class="aux-content-widget-2">
		  |            <div id="relatedListsWidget">
		  |                <div class="rightcornerlink">
		  |                    <a href="/list/create?ref_=tt_rls" >Create a list</a>&nbsp;&raquo;
		  |                </div>
		  |                <h3>User Lists</h3>
		  |                <p>Related lists from IMDb users</p>
		  |
		  |    <div class="list-preview even">
		  |        <div class="list-preview-item-narrow">
		  |<a href="/list/ls000527259?ref_=tt_rls_1" ><img height="86" width="86" alt="list image" title="list image"src="/images/nopicture/medium/film.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BMTY1NTI0ODUyOF5BMl5BanBnXkFtZTgwNTEyNjQ0MDE@._V1_SX86_CR0,0,86,86_AL_.jpg" /></a>        </div>
		  |        <div class="list_name">
		  |            <strong><a href="/list/ls000527259?ref_=tt_rls_1" >Movies Nominated for Best Animated Feature Film</a></strong>
		  |        </div>
		  |        <div class="list_meta">
		  |            a list of 44 titles
		  |            <br />created 25&nbsp;Mar&nbsp;2011
		  |        </div>
		  |        <div class="clear">&nbsp;</div>
		  |    </div>
		  |    <div class="list-preview odd">
		  |        <div class="list-preview-item-narrow">
		  |<a href="/list/ls006214255?ref_=tt_rls_2" ><img height="86" width="86" alt="list image" title="list image"src="/images/nopicture/medium/film.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BMTMxNTMwODM0NF5BMl5BanBnXkFtZTcwODAyMTk2Mw@@._V1_SX86_CR0,0,86,86_AL_.jpg" /></a>        </div>
		  |        <div class="list_name">
		  |            <strong><a href="/list/ls006214255?ref_=tt_rls_2" >Top 2008</a></strong>
		  |        </div>
		  |        <div class="list_meta">
		  |            a list of 41 titles
		  |            <br />created 03&nbsp;Jan&nbsp;2012
		  |        </div>
		  |        <div class="clear">&nbsp;</div>
		  |    </div>
		  |    <div class="list-preview even">
		  |        <div class="list-preview-item-narrow">
		  |<a href="/list/ls004949666?ref_=tt_rls_3" ><img height="86" width="86" alt="list image" title="list image"src="/images/nopicture/medium/film.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BMTM2Nzk5ODU5NF5BMl5BanBnXkFtZTcwMjQ1ODYxMQ@@._V1_SX86_CR0,0,86,86_AL_.jpg" /></a>        </div>
		  |        <div class="list_name">
		  |            <strong><a href="/list/ls004949666?ref_=tt_rls_3" >aj ' s animated favorites</a></strong>
		  |        </div>
		  |        <div class="list_meta">
		  |            a list of 30 titles
		  |            <br />created 22&nbsp;May&nbsp;2012
		  |        </div>
		  |        <div class="clear">&nbsp;</div>
		  |    </div>
		  |    <div class="list-preview odd">
		  |        <div class="list-preview-item-narrow">
		  |<a href="/list/ls050565990?ref_=tt_rls_4" ><img height="86" width="86" alt="list image" title="list image"src="/images/nopicture/medium/film.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BMTM5NzkxMzQ5MF5BMl5BanBnXkFtZTcwNDYwMTA3NA@@._V1_SX86_CR0,0,86,86_AL_.jpg" /></a>        </div>
		  |        <div class="list_name">
		  |            <strong><a href="/list/ls050565990?ref_=tt_rls_4" >Blu-ray</a></strong>
		  |        </div>
		  |        <div class="list_meta">
		  |            a list of 27 titles
		  |            <br />created 15&nbsp;Oct&nbsp;2012
		  |        </div>
		  |        <div class="clear">&nbsp;</div>
		  |    </div>
		  |    <div class="list-preview even">
		  |        <div class="list-preview-item-narrow">
		  |<a href="/list/ls050985609?ref_=tt_rls_5" ><img height="86" width="86" alt="list image" title="list image"src="/images/nopicture/medium/film.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BMTczNTI2ODUwOF5BMl5BanBnXkFtZTcwMTU0NTIzMw@@._V1_SX86_CR0,0,86,86_AL_.jpg" /></a>        </div>
		  |        <div class="list_name">
		  |            <strong><a href="/list/ls050985609?ref_=tt_rls_5" >Watched Movie List(2012)</a></strong>
		  |        </div>
		  |        <div class="list_meta">
		  |            a list of 33 titles
		  |            <br />created 01&nbsp;Dec&nbsp;2012
		  |        </div>
		  |        <div class="clear">&nbsp;</div>
		  |    </div>
		  |                <div class="see-more">
		  |                    <a href="/lists/tt0441773?ref_=tt_rls_sm" >See all related lists</a>&nbsp;&raquo;
		  |                </div>
		  |            </div>
		  |        </div>
		  |
		  |        
		  |	
		  |	<!-- no content received for slot: btf_rhs1 -->
		  |	
		  |
		  |
		  |    <div class="aux-content-widget-2">
		  |        <h3>Connect with IMDb</h3>
		  |        <div id="facebookWrapper">
		  |            <iframe
		  |                scrolling="no"
		  |                frameborder="0"
		  |                id="facebookIframe"
		  |                allowTransparency="true"></iframe>
		  |        </div>
		  |        <hr>
		  |        <iframe allowtransparency="true"
		  |            frameborder="0"
		  |            role="presentation"
		  |            scrolling="no"
		  |            id="twitterIframe"></iframe>
		  |    </div>
		  |
		  |
		  |
		  |  
		  |      
		  |    <div class="aux-content-widget-2">
		  |        <div id="ratingWidget">
		  |            <h3>Share this Rating</h3>
		  |            <p>
		  |                Title:
		  |                <strong>Kung Fu Panda</strong>
		  |                (2008)
		  |            </p>
		  |            <span class="imdbRatingPlugin imdbRatingStyle1" data-user="" data-title="tt0441773" data-style="t1">
		  |<a href="/title/tt0441773/?ref_=tt_plg_rt" > <img alt="Kung Fu Panda (2008) on IMDb"
		  |src="http://ia.media-imdb.com/images/G/01/imdb/images/plugins/imdb_46x22-2264473254._V379390954_.png">
		  |</a>                <span class="rating">7.6<span class="ofTen">/10</span></span>                
		  |<img src="http://ia.media-imdb.com/images/G/01/imdb/images/plugins/imdb_star_22x21-2889147855._V379391454_.png" class="star">
		  |            </span>
		  |            <p>Want to share IMDb's rating on your own site? Use the HTML below.</p>
		  |            <div id="ratingPluginHTML" class="hidden">
		  |                <div class="message_box small">
		  |                    <div class="error">
		  |                        <p>
		  |                        You must be a registered user to use the IMDb rating plugin.
		  |                        </p>
		  |                        <a href="/register/login?ref_=tt_plg_rt" class="cboxElement" rel='login'>Login</a>
		  |                    </div>
		  |                </div>
		  |            </div>
		  |            <div id="ratingWidgetLinks">
		  |                <span class="titlePageSprite arrows show"></span>
		  |                <a href="/plugins?titleId=tt0441773&ref_=tt_plg_rt" id="toggleRatingPluginHTML" itemprop='url' >Show HTML</a>
		  |                <a href="/plugins?titleId=tt0441773&ref_=tt_plg_rt" itemprop='url'>View more styles</a>
		  |            </div>
		  |        </div>
		  |    </div>
		  |
		  |
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_TitlePlayQuizWidget_started');
		  |    }
		  |  </script>
		  |    
		  |<div class="aux-content-widget-2 play_quiz_widget">
		  |    <h3>Take The Quiz!</h3>
		  |<a href="/games/guess/tt0441773?ref_=tt_qz" class="icon" ></a><span>Test your knowledge of <a href="/games/guess/tt0441773?ref_=tt_qz" >Kung Fu Panda</a>.</span>
		  |</div>
		  |    
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_TitlePlayQuizWidget_finished');
		  |    }
		  |  </script>
		  |
		  |
		  |<div class="aux-content-widget-2 poll-widget-rhs ">
		  |    <style>
		  |        .aux-content-widget-2.poll-widget-rhs ul li { margin-bottom: 0.5em; clear: left; font-weight: bold;}
		  |        .aux-content-widget-2.poll-widget-rhs span { margin-bottom: 0.5em; clear: left;}
		  |        .aux-content-widget-2.poll-widget-rhs img { float: left; padding: 0 5px 5px 0; height: 86px; width: 86px;}
		  |    </style>
		  |    <h3>User Polls</h3>
		  |    <ul>
		  |        <li>
		  |<a href="/poll/IyZeUraZGwE/?ref_=tt_po_i1" ><img height="86" width="86" alt="poll image" title="poll image"src="http://i.imdb.com/images/nopicture/140x209/unknown.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BMTYxNjY1ODQ3MF5BMl5BanBnXkFtZTcwMjgwMjk2Mw@@._V1_SY86_CR21,0,86,86_.jpg" /></a>        <a href="/poll/IyZeUraZGwE/?ref_=tt_po_q1" >The Chosen Ones</a>
		  |        <li>
		  |<a href="/poll/Z5bT-pBAwwg/?ref_=tt_po_i2" ><img height="86" width="86" alt="poll image" title="poll image"src="http://i.imdb.com/images/nopicture/140x209/unknown.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BMTIxOTY1NjUyN15BMl5BanBnXkFtZTcwMjMxMDk1MQ@@._V1_SX86_CR0,0,86,86_.jpg" /></a>        <a href="/poll/Z5bT-pBAwwg/?ref_=tt_po_q2" >Funny Martial Arts Movie Titles</a>
		  |        <li>
		  |<a href="/poll/HAg0Q51WptA/?ref_=tt_po_i3" ><img height="86" width="86" alt="poll image" title="poll image"src="http://i.imdb.com/images/nopicture/140x209/unknown.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BMTExNjI5NjAzODheQTJeQWpwZ15BbWU3MDU4OTM2MjM@._V1_SY86_CR28,0,86,86_.jpg" /></a>        <a href="/poll/HAg0Q51WptA/?ref_=tt_po_q3" >Best talking animals</a>
		  |        <li>
		  |<a href="/poll/AlfZW8OBW7M/?ref_=tt_po_i4" ><img height="86" width="86" alt="poll image" title="poll image"src="http://i.imdb.com/images/nopicture/140x209/unknown.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BMTMyODMzMTg5OV5BMl5BanBnXkFtZTcwNTE3OTk1Mg@@._V1_SY86_CR2,0,86,86_.jpg" /></a>        <a href="/poll/AlfZW8OBW7M/?ref_=tt_po_q4" >Unconventional Fight-Training</a>
		  |        <li>
		  |<a href="/poll/AyIs1g1JJcc/?ref_=tt_po_i5" ><img height="86" width="86" alt="poll image" title="poll image"src="http://i.imdb.com/images/nopicture/140x209/unknown.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BMjExMTEzODkyN15BMl5BanBnXkFtZTcwNTU4NTc4OQ@@._V1_SX86_CR0,0,86,86_.jpg" /></a>        <a href="/poll/AyIs1g1JJcc/?ref_=tt_po_q5" >Hans Zimmer's Best Scores</a>
		  |    </ul>
		  |    <div class="see-more"><a href="/poll/?ref_=tt_po_sm" >See more polls &raquo;</a></div>
		  |</div>
		  |
		  |       
		  |	
		  |	<!-- no content received for slot: bottom_rhs -->
		  |	
		  |        
		  |
		  |	
		  |	<!-- begin BTF_RHS2 -->
		  |<div id="btf_rhs2_wrapper" class="dfp_slot">
		  |<script type="text/javascript">
		  |doWithAds(function(){
		  |ad_utils.register_ad('btf_rhs2');
		  |});
		  |</script>
		  |<iframe data-dart-params="#imdb2.consumer.title/maindetails;!TILE!;sz=300x250,16x1;p=br2;g=fm;g=co;g=an;g=ac;g=ad;g=baa;tt=f;m=PG;mh=PG;ml=PG;coo=us;fv=1;id=tt0441773;ab=c;bpx=2;md=tt0441773;s=3075;s=32;s=1009;s=3717;oe=utf-8;[CLIENT_SIDE_KEYVALUES];u=065068742621;ord=065068742621?" id="btf_rhs2" name="btf_rhs2" class="yesScript" width="300" height="250" data-original-width="300" data-original-height="250" data-config-width="300" data-config-height="250" data-cookie-width="null" data-cookie-height="null" marginwidth="0" marginheight="0" frameborder="0" scrolling="no" allowtransparency="true" onload="doWithAds.call(this, function(){ ad_utils.on_ad_load(this); });"></iframe>
		  |<noscript><a href="http://ad.doubleclick.net/N4215/jump/imdb2.consumer.title/maindetails;tile=2;sz=300x250,16x1;p=br2;g=fm;g=co;g=an;g=ac;g=ad;g=baa;tt=f;m=PG;mh=PG;ml=PG;coo=us;fv=1;id=tt0441773;ab=c;bpx=2;md=tt0441773;s=3075;s=32;s=1009;s=3717;ord=065068742621?" target="_blank"><img src="http://ad.doubleclick.net/N4215/ad/imdb2.consumer.title/maindetails;tile=2;sz=300x250,16x1;p=br2;g=fm;g=co;g=an;g=ac;g=ad;g=baa;tt=f;m=PG;mh=PG;ml=PG;coo=us;fv=1;id=tt0441773;ab=c;bpx=2;md=tt0441773;s=3075;s=32;s=1009;s=3717;ord=065068742621?" border="0" alt="advertisement" /></a></noscript>
		  |</div>
		  |<div id="btf_rhs2_reflow_helper"></div>
		  |<script>
		  |doWithAds(function(){
		  |ad_utils.gpt.render_ad('btf_rhs2');
		  |}, "ad_utils not defined, unable to render client-side GPT ad.");
		  |</script>
		  |<!-- End BTF_RHS2 -->
		  |	
		  |
		  |        
		  |    </div> 
		  |    
		  |    <div id="maindetails_center_bottom" class="maindetails_center">
		  |        
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_TitleEpisodesWidget_started');
		  |    }
		  |  </script>
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_TitleEpisodesWidget_finished');
		  |    }
		  |  </script>
		  |            
		  |        
		  |          <div class="article highlighted" id="titleAwardsRanks">
		  |
		  |            
		  |
		  |            
		  |        <span itemprop="awards"><b>Nominated for 1 Oscar.</b></span>
		  |        <span itemprop="awards">Another 16 wins & 23 nominations.</span>
		  |    <span class="see-more inline">
		  |<a href="/title/tt0441773/awards?ref_=tt_awd" >See more awards</a>&nbsp;&raquo;    </span>
		  |          </div>
		  |         
		  |            
		  |        
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_TitleMediaStripWidget_started');
		  |    }
		  |  </script>
		  |        
		  |        
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_TitleMediaStripWidget_started');
		  |    }
		  |  </script>
		  |    
		  |    
		  |    
		  |
		  |      <div class = "article" id="titleMediaStrip">
		  |        <div class="mediastrip_container combined">
		  |                <div id="combined-videos">
		  |                    <h2>Videos</h2>
		  |    <div class="mediastrip_big">
		  |                <span class="video_slate" itemscope itemtype="http://schema.org/videoObject">
		  |                <meta itemprop="duration" content="T91M0S" />
		  |
		  |
		  |
		  |
		  |<a onclick="window.open(this.href+'','_blank');return false;" href="/video/amazon/vi943697177/offsite?page-action=watch-aiv&ref_=tt_pv_vi_1" title="Kung Fu Panda -- Stars Jack Black as an unlikely &quot;chosen one,&quot; who is trained by the &quot;furious five&quot; martial arts masters (Angelina Jolie, Dustin Hoffman, Jackie Chan, Lucy Liu and Ian McShane) to become the Kung Fu Panda." alt="Kung Fu Panda -- Stars Jack Black as an unlikely &quot;chosen one,&quot; who is trained by the &quot;furious five&quot; martial arts masters (Angelina Jolie, Dustin Hoffman, Jackie Chan, Lucy Liu and Ian McShane) to become the Kung Fu Panda." class=" " data-video="vi943697177" data-context="amazon" data-rid="1AS3C1K4E3G5KR01FPMR" widget-context="titleMaindetails" itemprop="url" ><img height="105" width="139" alt="Kung Fu Panda -- Stars Jack Black as an unlikely &quot;chosen one,&quot; who is trained by the &quot;furious five&quot; martial arts masters (Angelina Jolie, Dustin Hoffman, Jackie Chan, Lucy Liu and Ian McShane) to become the Kung Fu Panda." title="Kung Fu Panda -- Stars Jack Black as an unlikely &quot;chosen one,&quot; who is trained by the &quot;furious five&quot; martial arts masters (Angelina Jolie, Dustin Hoffman, Jackie Chan, Lucy Liu and Ian McShane) to become the Kung Fu Panda."src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/small/no-video-slate-856072904._V379390253_.png"class="loadlate hidden video" loadlate="http://ia.media-imdb.com/images/M/MV5BMTIxOTY1NjUyN15BMl5BanBnXkFtZTcwMjMxMDk1MQ@@._V1_BR-65_CT-15_SP229,229,0,C,0,0,0_CR45,62,139,105_PIimdb-blackband-204-14,TopLeft,0,0_PIimdb-blackband-204-28,BottomLeft,0,1_CR0,0,139,105_PIimdb-goldbutton-big,BottomRight,-1,-1_ZAFull%2520Movie,2,76,16,137,verdenab,8,255,255,255,1_ZAat%2520Amazon%2520%25BB,2,1,14,137,verdenab,7,255,255,255,1_ZA91:00,103,1,14,36,verdenab,7,255,255,255,1_.jpg" itemprop='image' viconst="vi943697177" /></a>            </span>
		  |                <span class="video_slate_last" itemscope itemtype="http://schema.org/videoObject">
		  |                <meta itemprop="duration" content="T1M11S" />
		  |
		  |
		  |
		  |
		  |<a href="/video/screenplay/vi2498298137?ref_=tt_pv_vi_2" title="Kung Fu Panda -- Clip: Nothing is impossible" alt="Kung Fu Panda -- Clip: Nothing is impossible" class="video-colorbox" data-video="vi2498298137" data-context="screenplay" data-rid="1AS3C1K4E3G5KR01FPMR" widget-context="titleMaindetails" itemprop="url" ><img height="105" width="139" alt="Kung Fu Panda -- Clip: Nothing is impossible" title="Kung Fu Panda -- Clip: Nothing is impossible"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/small/no-video-slate-856072904._V379390253_.png"class="loadlate hidden video" loadlate="http://ia.media-imdb.com/images/M/MV5BMTI2NTQ5NjkxNV5BMl5BanBnXkFtZTcwNDA4ODY3MQ@@._V1_SP229,229,0,C,0,0,0_CR45,62,139,105_PIimdb-blackband-204-14,TopLeft,0,0_PIimdb-blackband-204-28,BottomLeft,0,1_CR0,0,139,105_PIimdb-bluebutton-big,BottomRight,-1,-1_ZAClip,2,76,16,137,verdenab,8,255,255,255,1_ZAon%2520IMDb,2,1,14,137,verdenab,7,255,255,255,1_ZA01:11,103,1,14,36,verdenab,7,255,255,255,1_.jpg" itemprop='image' viconst="vi2498298137" /></a>            </span>
		  |    </div>
		  |                </div>
		  |                <div id="combined-photos">
		  |                    <h2>Photos</h2>
		  |        <div class="mediastrip">
		  |                
		  |<a href="/media/rm2978331392/tt0441773?ref_=tt_pv_md_1" itemprop='thumbnailUrl'><img height="105" width="105" alt="Still of Angelina Jolie in Kung Fu Panda (2008)" title="Still of Angelina Jolie in Kung Fu Panda (2008)"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/small/unknown-1394846836._V379391227_.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BODc1MjU5MjEzOF5BMl5BanBnXkFtZTcwNjkyMzA3NA@@._V1_SX105_CR0,0,105,105_AL_.jpg" itemprop='image' /></a>                
		  |<a href="/media/rm2995108608/tt0441773?ref_=tt_pv_md_2" itemprop='thumbnailUrl'><img height="105" width="105" alt="Kung Fu Panda (2008)" title="Kung Fu Panda (2008)"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/small/unknown-1394846836._V379391227_.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BMTEyMzQ2OTAxNjVeQTJeQWpwZ15BbWU3MDQ5MjMwNzQ@._V1_SY105_CR1,0,105,105_AL_.jpg" itemprop='image' /></a>                
		  |<a href="/media/rm1342933504/tt0441773?ref_=tt_pv_md_3" itemprop='thumbnailUrl'><img height="105" width="105" alt="Seth Green and Jennifer Yuh at event of Kung Fu Panda (2008)" title="Seth Green and Jennifer Yuh at event of Kung Fu Panda (2008)"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/small/unknown-1394846836._V379391227_.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BMjA0NjE4MTE3OV5BMl5BanBnXkFtZTcwMzcwNzkyMg@@._V1_SY105_CR16,0,105,105_AL_.jpg" itemprop='image' /></a>        </div>
		  |                </div>
		  |            <div class="combined-see-more see-more">
		  |<a href="/title/tt0441773/mediaindex?ref_=tt_pv_mi_sm" ><span class="titlePageSprite showAllVidsAndPics"></span></a>
		  |<a href="/title/tt0441773/mediaindex?ref_=tt_pv_mi_sm" >93 photos</a>                
		  |<span class="ghost">|</span>        
		  |<a href="/title/tt0441773/videogallery?ref_=tt_pv_vi_sm" >19 videos</a>                
		  |<span class="ghost">|</span>        
		  |<a href="/title/tt0441773/news?ref_=tt_pv_nw_sm" >2305 news articles</a> &raquo;            </div>
		  |        </div>
		  |      </div>  
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_TitleMediaStripWidget_finished');
		  |    }
		  |  </script>
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_TitleMediaStripWidget_finished');
		  |    }
		  |  </script>
		  |
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_TitleRecsWidget_started');
		  |    }
		  |  </script>
		  |    
		  |        <div class="article" id="titleRecs">
		  |            <span class="rightcornerlink">
		  |            <a href="/help/show_leaf?personalrecommendations&ref_=tt_rec_lm" >Learn more</a>
		  |            </span>
		  |            
		  |            <h2 class="rec_heading_wrapper">  
		  |                <span class="rec_heading" data-spec="p13nsims:tt0441773">People who liked this also liked...&nbsp;</span>
		  |            </h2>
		  |
		  |            <div class="rec_wrapper" id="title_recs"
		  |                data-items-per-request="24"
		  |                data-items-per-page="6"
		  |                data-specs="p13nsims:tt0441773"
		  |                data-caller-name="p13nsims-title">      
		  |                
		  |    <div class="rec_const_picker">
		  |        <div class="rec_view">
		  |            <div class="rec_grave" style="display:none"></div>
		  |            <div class="rec_slide">
		  |                        <div class="rec_page">         
		  |    
		  |    
		  |    <div class="rec_item" data-info="" data-spec="p13nsims:tt0441773" data-tconst="tt1302011">
		  |        <div class="rec_overlay">
		  |            <div class="rec_filter"></div>
		  |            <div class="glyph rec_watchlist_glyph"></div>
		  |            <div class="glyph rec_blocked_glyph"></div>
		  |            <div class="glyph rec_rating_glyph"></div>
		  |            <div class="glyph rec_pending_glyph"></div>                        
		  |        </div>            
		  |<a href="/title/tt1302011/?ref_=tt_rec_tti" ><img height="113" width="76" alt="Kung Fu Panda 2" title="Kung Fu Panda 2"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/small/film-293970583._V379390468_.png"class="loadlate hidden rec_poster_img" loadlate="http://ia.media-imdb.com/images/M/MV5BMTg4MTQ3NTI3Nl5BMl5BanBnXkFtZTcwNzEzODQ2NA@@._V1_SX76_CR0,0,76,113_AL_.jpg" /> <br/>
		  |</a>    </div>
		  |
		  |    
		  |    
		  |    <div class="rec_item" data-info="" data-spec="p13nsims:tt0441773" data-tconst="tt1001526">
		  |        <div class="rec_overlay">
		  |            <div class="rec_filter"></div>
		  |            <div class="glyph rec_watchlist_glyph"></div>
		  |            <div class="glyph rec_blocked_glyph"></div>
		  |            <div class="glyph rec_rating_glyph"></div>
		  |            <div class="glyph rec_pending_glyph"></div>                        
		  |        </div>            
		  |<a href="/title/tt1001526/?ref_=tt_rec_tti" ><img height="113" width="76" alt="Megamind" title="Megamind"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/small/film-293970583._V379390468_.png"class="loadlate hidden rec_poster_img" loadlate="http://ia.media-imdb.com/images/M/MV5BMTAzMzI0NTMzNDBeQTJeQWpwZ15BbWU3MDM3NTAyOTM@._V1_SX76_CR0,0,76,113_AL_.jpg" /> <br/>
		  |</a>    </div>
		  |
		  |    
		  |    
		  |    <div class="rec_item" data-info="" data-spec="p13nsims:tt0441773" data-tconst="tt0479952">
		  |        <div class="rec_overlay">
		  |            <div class="rec_filter"></div>
		  |            <div class="glyph rec_watchlist_glyph"></div>
		  |            <div class="glyph rec_blocked_glyph"></div>
		  |            <div class="glyph rec_rating_glyph"></div>
		  |            <div class="glyph rec_pending_glyph"></div>                        
		  |        </div>            
		  |<a href="/title/tt0479952/?ref_=tt_rec_tti" ><img height="113" width="76" alt="Madagascar: Escape 2 Africa" title="Madagascar: Escape 2 Africa"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/small/film-293970583._V379390468_.png"class="loadlate hidden rec_poster_img" loadlate="http://ia.media-imdb.com/images/M/MV5BMjExMDA4NDcwMl5BMl5BanBnXkFtZTcwODAxNTQ3MQ@@._V1_SY113_CR0,0,76,113_AL_.jpg" /> <br/>
		  |</a>    </div>
		  |
		  |    
		  |    
		  |    <div class="rec_item" data-info="" data-spec="p13nsims:tt0441773" data-tconst="tt1080016">
		  |        <div class="rec_overlay">
		  |            <div class="rec_filter"></div>
		  |            <div class="glyph rec_watchlist_glyph"></div>
		  |            <div class="glyph rec_blocked_glyph"></div>
		  |            <div class="glyph rec_rating_glyph"></div>
		  |            <div class="glyph rec_pending_glyph"></div>                        
		  |        </div>            
		  |<a href="/title/tt1080016/?ref_=tt_rec_tti" ><img height="113" width="76" alt="Ice Age: Dawn of the Dinosaurs" title="Ice Age: Dawn of the Dinosaurs"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/small/film-293970583._V379390468_.png"class="loadlate hidden rec_poster_img" loadlate="http://ia.media-imdb.com/images/M/MV5BMjA4NDI0Mjg4NV5BMl5BanBnXkFtZTcwOTM1NTY0Mg@@._V1_SY113_CR2,0,76,113_AL_.jpg" /> <br/>
		  |</a>    </div>
		  |
		  |    
		  |    
		  |    <div class="rec_item" data-info="" data-spec="p13nsims:tt0441773" data-tconst="tt0892782">
		  |        <div class="rec_overlay">
		  |            <div class="rec_filter"></div>
		  |            <div class="glyph rec_watchlist_glyph"></div>
		  |            <div class="glyph rec_blocked_glyph"></div>
		  |            <div class="glyph rec_rating_glyph"></div>
		  |            <div class="glyph rec_pending_glyph"></div>                        
		  |        </div>            
		  |<a href="/title/tt0892782/?ref_=tt_rec_tti" ><img height="113" width="76" alt="Monsters vs. Aliens" title="Monsters vs. Aliens"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/small/film-293970583._V379390468_.png"class="loadlate hidden rec_poster_img" loadlate="http://ia.media-imdb.com/images/M/MV5BMTY0OTQ3MzE3MV5BMl5BanBnXkFtZTcwMDQyMzMzMg@@._V1_SY113_CR0,0,76,113_AL_.jpg" /> <br/>
		  |</a>    </div>
		  |
		  |    
		  |    
		  |    <div class="rec_item" data-info="" data-spec="p13nsims:tt0441773" data-tconst="tt1690953">
		  |        <div class="rec_overlay">
		  |            <div class="rec_filter"></div>
		  |            <div class="glyph rec_watchlist_glyph"></div>
		  |            <div class="glyph rec_blocked_glyph"></div>
		  |            <div class="glyph rec_rating_glyph"></div>
		  |            <div class="glyph rec_pending_glyph"></div>                        
		  |        </div>            
		  |<a href="/title/tt1690953/?ref_=tt_rec_tti" ><img height="113" width="76" alt="Despicable Me 2" title="Despicable Me 2"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/small/film-293970583._V379390468_.png"class="loadlate hidden rec_poster_img" loadlate="http://ia.media-imdb.com/images/M/MV5BMjExNjAyNTcyMF5BMl5BanBnXkFtZTgwODQzMjQ3MDE@._V1_SY113_CR0,0,76,113_AL_.jpg" /> <br/>
		  |</a>    </div>
		  |
		  |                        </div>
		  |                        <div class="rec_page">         
		  |    
		  |    
		  |    <div class="rec_item" data-info="" data-spec="p13nsims:tt0441773" data-tconst="tt0844471">
		  |        <div class="rec_overlay">
		  |            <div class="rec_filter"></div>
		  |            <div class="glyph rec_watchlist_glyph"></div>
		  |            <div class="glyph rec_blocked_glyph"></div>
		  |            <div class="glyph rec_rating_glyph"></div>
		  |            <div class="glyph rec_pending_glyph"></div>                        
		  |        </div>            
		  |<a href="/title/tt0844471/?ref_=tt_rec_tti" ><img height="113" width="76" alt="Cloudy with a Chance of Meatballs" title="Cloudy with a Chance of Meatballs"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/small/film-293970583._V379390468_.png"class="loadlate hidden rec_poster_img" loadlate="http://ia.media-imdb.com/images/M/MV5BMTg0MjAwNDI5MV5BMl5BanBnXkFtZTcwODkyMzg2Mg@@._V1_SY113_CR0,0,76,113_AL_.jpg" /> <br/>
		  |</a>    </div>
		  |
		  |    
		  |    
		  |    <div class="rec_item" data-info="" data-spec="p13nsims:tt0441773" data-tconst="tt0120630">
		  |        <div class="rec_overlay">
		  |            <div class="rec_filter"></div>
		  |            <div class="glyph rec_watchlist_glyph"></div>
		  |            <div class="glyph rec_blocked_glyph"></div>
		  |            <div class="glyph rec_rating_glyph"></div>
		  |            <div class="glyph rec_pending_glyph"></div>                        
		  |        </div>            
		  |<a href="/title/tt0120630/?ref_=tt_rec_tti" ><img height="113" width="76" alt="Chicken Run" title="Chicken Run"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/small/film-293970583._V379390468_.png"class="loadlate hidden rec_poster_img" loadlate="http://ia.media-imdb.com/images/M/MV5BMTk1MjE3MjQ0OF5BMl5BanBnXkFtZTcwMTcyMTcyMQ@@._V1_SY113_CR2,0,76,113_AL_.jpg" /> <br/>
		  |</a>    </div>
		  |
		  |    
		  |    
		  |    <div class="rec_item" data-info="" data-spec="p13nsims:tt0441773" data-tconst="tt0307453">
		  |        <div class="rec_overlay">
		  |            <div class="rec_filter"></div>
		  |            <div class="glyph rec_watchlist_glyph"></div>
		  |            <div class="glyph rec_blocked_glyph"></div>
		  |            <div class="glyph rec_rating_glyph"></div>
		  |            <div class="glyph rec_pending_glyph"></div>                        
		  |        </div>            
		  |<a href="/title/tt0307453/?ref_=tt_rec_tti" ><img height="113" width="76" alt="Shark Tale" title="Shark Tale"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/small/film-293970583._V379390468_.png"class="loadlate hidden rec_poster_img" loadlate="http://ia.media-imdb.com/images/M/MV5BMTMxMjY0NzE2M15BMl5BanBnXkFtZTcwNTc3ODcyMw@@._V1_SY113_CR0,0,76,113_AL_.jpg" /> <br/>
		  |</a>    </div>
		  |
		  |    
		  |    
		  |    <div class="rec_item" data-info="" data-spec="p13nsims:tt0441773" data-tconst="tt0837562">
		  |        <div class="rec_overlay">
		  |            <div class="rec_filter"></div>
		  |            <div class="glyph rec_watchlist_glyph"></div>
		  |            <div class="glyph rec_blocked_glyph"></div>
		  |            <div class="glyph rec_rating_glyph"></div>
		  |            <div class="glyph rec_pending_glyph"></div>                        
		  |        </div>            
		  |<a href="/title/tt0837562/?ref_=tt_rec_tti" ><img height="113" width="76" alt="Hotel Transylvania" title="Hotel Transylvania"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/small/film-293970583._V379390468_.png"class="loadlate hidden rec_poster_img" loadlate="http://ia.media-imdb.com/images/M/MV5BMTM3NjQyODI3M15BMl5BanBnXkFtZTcwMDM4NjM0OA@@._V1_SY113_CR0,0,76,113_AL_.jpg" /> <br/>
		  |</a>    </div>
		  |
		  |    
		  |    
		  |    <div class="rec_item" data-info="" data-spec="p13nsims:tt0441773" data-tconst="tt0119715">
		  |        <div class="rec_overlay">
		  |            <div class="rec_filter"></div>
		  |            <div class="glyph rec_watchlist_glyph"></div>
		  |            <div class="glyph rec_blocked_glyph"></div>
		  |            <div class="glyph rec_rating_glyph"></div>
		  |            <div class="glyph rec_pending_glyph"></div>                        
		  |        </div>            
		  |<a href="/title/tt0119715/?ref_=tt_rec_tti" ><img height="113" width="76" alt="Mousehunt" title="Mousehunt"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/small/film-293970583._V379390468_.png"class="loadlate hidden rec_poster_img" loadlate="http://ia.media-imdb.com/images/M/MV5BMTk1MDExMTA5NF5BMl5BanBnXkFtZTcwOTMzNTIyMQ@@._V1_SY113_CR1,0,76,113_AL_.jpg" /> <br/>
		  |</a>    </div>
		  |
		  |    
		  |    
		  |    <div class="rec_item" data-info="" data-spec="p13nsims:tt0441773" data-tconst="tt1323594">
		  |        <div class="rec_overlay">
		  |            <div class="rec_filter"></div>
		  |            <div class="glyph rec_watchlist_glyph"></div>
		  |            <div class="glyph rec_blocked_glyph"></div>
		  |            <div class="glyph rec_rating_glyph"></div>
		  |            <div class="glyph rec_pending_glyph"></div>                        
		  |        </div>            
		  |<a href="/title/tt1323594/?ref_=tt_rec_tti" ><img height="113" width="76" alt="Despicable Me" title="Despicable Me"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/small/film-293970583._V379390468_.png"class="loadlate hidden rec_poster_img" loadlate="http://ia.media-imdb.com/images/M/MV5BMTY3NjY0MTQ0Nl5BMl5BanBnXkFtZTcwMzQ2MTc0Mw@@._V1_SY113_CR0,0,76,113_AL_.jpg" /> <br/>
		  |</a>    </div>
		  |
		  |                        </div>
		  |            </div>
		  |            <div class="rec_nav">
		  |                <div class="rec_nav_page_num"></div>
		  |                <a class="rec_nav_left">&#9668; Prev 6</a>
		  |                <a class="rec_nav_right">Next 6 &#9658;</a>
		  |            </div>
		  |        </div>
		  |    </div>
		  |    
		  |   <div class="rec_overviews">
		  |
		  |         
		  |      <div class="rec_overview" data-tconst="tt1302011">
		  |    
		  |    
		  |    
		  |    <div class="rec_poster" data-info="" data-spec="p13nsims:tt0441773" data-tconst="tt1302011">
		  |        <div class="rec_overlay">
		  |            <div class="rec_filter"></div>
		  |            <div class="glyph rec_watchlist_glyph"></div>
		  |            <div class="glyph rec_blocked_glyph"></div>
		  |            <div class="glyph rec_rating_glyph"></div>
		  |            <div class="glyph rec_pending_glyph"></div>                        
		  |        </div>            
		  |<a href="/title/tt1302011/?ref_=tt_rec_tti" ><img height="190" width="128" alt="Kung Fu Panda 2" title="Kung Fu Panda 2"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/large/film-184890147._V379391879_.png"class="loadlate hidden rec_poster_img" loadlate="http://ia.media-imdb.com/images/M/MV5BMTg4MTQ3NTI3Nl5BMl5BanBnXkFtZTcwNzEzODQ2NA@@._V1_SX128_CR0,0,128,190_AL_.jpg" /> <br/>
		  |</a>    </div>
		  |
		  |    
		  |       <div class="rec_actions">
		  |     
		  |         <div class="rec_action_divider">
		  |           <div class="wlb_classic_wrapper">
		  |             <span class="wlb_wrapper">
		  |               <a class="rec_wlb_watchlist_btn" data-tconst="tt1302011" data-size="medium" data-caller-name="p13nsims-title" data-type="primary"></a>
		  |             </span>
		  |           </div>  
		  |         </div> 
		  |       
		  |         <div class="rec_action_divider">
		  |           <span class="btn2_wrapper">
		  |             <a class="rec_next rec_half_button btn2 medium btn2_text_on" title="Show me the next title" onclick="">
		  |               <span class="btn2_glyph">0</span>
		  |               <span class="btn2_text">Next &raquo;</span>
		  |             </a>
		  |           </span>
		  |         </div>    
		  |         
		  |             <input type="hidden" name="49e6c" value="e341">
		  |       </div>
		  |       
		  |       <div class="rec_details">
		  |         <div class="rec-info">
		  |         
		  |           <div class="rec-jaw-upper">  
		  |
		  |     <div class="rec-title">
		  |       <a href="/title/tt1302011/?ref_=tt_rec_tt" ><b>Kung Fu Panda 2</b></a>
		  |            <span class="nobr">(2011)</span>
		  |   </div>  
		  |             
		  |
		  |
		  |    <div class="rec-cert-genre rec-elipsis">
		  |        
		  |
		  |            
		  |            
		  |                
		  |                    <span title="Ratings certificate for Kung Fu Panda 2 (2011)"
		  |                          class="us_pg titlePageSprite absmiddle"></span>
		  |
		  |                     Animation          
		  | <span class="ghost">|</span> 
		  |                     Action          
		  | <span class="ghost">|</span> 
		  |                     Adventure          
		  |
		  |        
		  |    </div>
		  |             
		  |             <div class="rec-rating">
		  |               
		  |
		  |  
		  |
		  |  
		  |<div class="rating rating-list" data-starbar-class="rating-list" data-auth="" data-user="" id="tt1302011|imdb|7.3|7.3|p13nsims-title|tt0441773|title|main" data-ga-identifier=""
		  |title="Users rated this 7.3/10 (121,321 votes) - click stars to rate" >
		  |<span class="rating-bg">&nbsp;</span>
		  |<span class="rating-imdb " style="width: 102.2px">&nbsp;</span>
		  |<span class="rating-stars">
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>1</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>2</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>3</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>4</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>5</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>6</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>7</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>8</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>9</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>10</span></a>
		  |</span>
		  |<span class="rating-rating "><span class="value">7.3</span><span class="grey">/</span><span class="grey">10</span></span>
		  |<span class="rating-cancel "><a href="/title/tt1302011/vote?v=X;k=" title="Delete" rel="nofollow"><span>X</span></a></span>
		  |&nbsp;</div>
		  |
		  |               <div class="rec-outline">
		  |    <p>
		  |Po and his friends fight to stop a peacock villain from conquering China with a deadly new weapon, but first the Dragon Warrior must come to terms with his past.    </p>
		  |               </div>
		  |               
		  |             </div>  
		  |             
		  |           </div>
		  |           
		  |           <div class="rec-jaw-lower">
		  |             <div class="rec-jaw-teeth"></div>                       
		  | <div class="rec-director rec-ellipsis">
		  |      <b>Director:</b>
		  |Jennifer Yuh  </div>
		  |<div class="rec-actor rec-ellipsis"> <span>
		  |    <b>Stars:</b> 
		  |Jack Black, 
		  |Angelina Jolie, 
		  |Jackie Chan</span></div> 
		  |           </div>
		  |           
		  |         </div>
		  |       </div>
		  |
		  |      </div>
		  |     
		  |         
		  |      <div class="rec_overview" data-tconst="tt1001526">
		  |    
		  |    
		  |    
		  |    <div class="rec_poster" data-info="" data-spec="p13nsims:tt0441773" data-tconst="tt1001526">
		  |        <div class="rec_overlay">
		  |            <div class="rec_filter"></div>
		  |            <div class="glyph rec_watchlist_glyph"></div>
		  |            <div class="glyph rec_blocked_glyph"></div>
		  |            <div class="glyph rec_rating_glyph"></div>
		  |            <div class="glyph rec_pending_glyph"></div>                        
		  |        </div>            
		  |<a href="/title/tt1001526/?ref_=tt_rec_tti" ><img height="190" width="128" alt="Megamind" title="Megamind"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/large/film-184890147._V379391879_.png"class="loadlate hidden rec_poster_img" loadlate="http://ia.media-imdb.com/images/M/MV5BMTAzMzI0NTMzNDBeQTJeQWpwZ15BbWU3MDM3NTAyOTM@._V1_SX128_CR0,0,128,190_AL_.jpg" /> <br/>
		  |</a>    </div>
		  |
		  |    
		  |       <div class="rec_actions">
		  |     
		  |         <div class="rec_action_divider">
		  |           <div class="wlb_classic_wrapper">
		  |             <span class="wlb_wrapper">
		  |               <a class="rec_wlb_watchlist_btn" data-tconst="tt1001526" data-size="medium" data-caller-name="p13nsims-title" data-type="primary"></a>
		  |             </span>
		  |           </div>  
		  |         </div> 
		  |       
		  |         <div class="rec_action_divider">
		  |           <span class="btn2_wrapper">
		  |             <a class="rec_next rec_half_button btn2 medium btn2_text_on" title="Show me the next title" onclick="">
		  |               <span class="btn2_glyph">0</span>
		  |               <span class="btn2_text">Next &raquo;</span>
		  |             </a>
		  |           </span>
		  |         </div>    
		  |         
		  |             <input type="hidden" name="49e6c" value="e341">
		  |       </div>
		  |       
		  |       <div class="rec_details">
		  |         <div class="rec-info">
		  |         
		  |           <div class="rec-jaw-upper">  
		  |
		  |     <div class="rec-title">
		  |       <a href="/title/tt1001526/?ref_=tt_rec_tt" ><b>Megamind</b></a>
		  |            <span class="nobr">(2010)</span>
		  |   </div>  
		  |             
		  |
		  |
		  |    <div class="rec-cert-genre rec-elipsis">
		  |        
		  |
		  |            
		  |            
		  |                
		  |                    <span title="Ratings certificate for Megamind (2010)"
		  |                          class="us_pg titlePageSprite absmiddle"></span>
		  |
		  |                     Animation          
		  | <span class="ghost">|</span> 
		  |                     Action          
		  | <span class="ghost">|</span> 
		  |                     Comedy          
		  |
		  |        
		  |    </div>
		  |             
		  |             <div class="rec-rating">
		  |               
		  |
		  |  
		  |
		  |  
		  |<div class="rating rating-list" data-starbar-class="rating-list" data-auth="" data-user="" id="tt1001526|imdb|7.3|7.3|p13nsims-title|tt0441773|title|main" data-ga-identifier=""
		  |title="Users rated this 7.3/10 (124,184 votes) - click stars to rate" >
		  |<span class="rating-bg">&nbsp;</span>
		  |<span class="rating-imdb " style="width: 102.2px">&nbsp;</span>
		  |<span class="rating-stars">
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>1</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>2</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>3</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>4</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>5</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>6</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>7</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>8</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>9</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>10</span></a>
		  |</span>
		  |<span class="rating-rating "><span class="value">7.3</span><span class="grey">/</span><span class="grey">10</span></span>
		  |<span class="rating-cancel "><a href="/title/tt1001526/vote?v=X;k=" title="Delete" rel="nofollow"><span>X</span></a></span>
		  |&nbsp;</div>
		  |
		  |               <div class="rec-outline">
		  |    <p>
		  |The supervillain Megamind finally defeats his nemesis, the superhero Metro Man. But without a hero, he loses all purpose and must find new meaning to his life.    </p>
		  |               </div>
		  |               
		  |             </div>  
		  |             
		  |           </div>
		  |           
		  |           <div class="rec-jaw-lower">
		  |             <div class="rec-jaw-teeth"></div>                       
		  | <div class="rec-director rec-ellipsis">
		  |      <b>Director:</b>
		  |Tom McGrath  </div>
		  |<div class="rec-actor rec-ellipsis"> <span>
		  |    <b>Stars:</b> 
		  |Will Ferrell, 
		  |Jonah Hill, 
		  |Brad Pitt</span></div> 
		  |           </div>
		  |           
		  |         </div>
		  |       </div>
		  |
		  |      </div>
		  |     
		  |         
		  |      <div class="rec_overview" data-tconst="tt0479952">
		  |    
		  |    
		  |    
		  |    <div class="rec_poster" data-info="" data-spec="p13nsims:tt0441773" data-tconst="tt0479952">
		  |        <div class="rec_overlay">
		  |            <div class="rec_filter"></div>
		  |            <div class="glyph rec_watchlist_glyph"></div>
		  |            <div class="glyph rec_blocked_glyph"></div>
		  |            <div class="glyph rec_rating_glyph"></div>
		  |            <div class="glyph rec_pending_glyph"></div>                        
		  |        </div>            
		  |<a href="/title/tt0479952/?ref_=tt_rec_tti" ><img height="190" width="128" alt="Madagascar: Escape 2 Africa" title="Madagascar: Escape 2 Africa"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/large/film-184890147._V379391879_.png"class="loadlate hidden rec_poster_img" loadlate="http://ia.media-imdb.com/images/M/MV5BMjExMDA4NDcwMl5BMl5BanBnXkFtZTcwODAxNTQ3MQ@@._V1_SY190_CR0,0,128,190_AL_.jpg" /> <br/>
		  |</a>    </div>
		  |
		  |    
		  |       <div class="rec_actions">
		  |     
		  |         <div class="rec_action_divider">
		  |           <div class="wlb_classic_wrapper">
		  |             <span class="wlb_wrapper">
		  |               <a class="rec_wlb_watchlist_btn" data-tconst="tt0479952" data-size="medium" data-caller-name="p13nsims-title" data-type="primary"></a>
		  |             </span>
		  |           </div>  
		  |         </div> 
		  |       
		  |         <div class="rec_action_divider">
		  |           <span class="btn2_wrapper">
		  |             <a class="rec_next rec_half_button btn2 medium btn2_text_on" title="Show me the next title" onclick="">
		  |               <span class="btn2_glyph">0</span>
		  |               <span class="btn2_text">Next &raquo;</span>
		  |             </a>
		  |           </span>
		  |         </div>    
		  |         
		  |             <input type="hidden" name="49e6c" value="e341">
		  |       </div>
		  |       
		  |       <div class="rec_details">
		  |         <div class="rec-info">
		  |         
		  |           <div class="rec-jaw-upper">  
		  |
		  |     <div class="rec-title">
		  |       <a href="/title/tt0479952/?ref_=tt_rec_tt" ><b>Madagascar: Escape 2 Africa</b></a>
		  |            <span class="nobr">(2008)</span>
		  |   </div>  
		  |             
		  |
		  |
		  |    <div class="rec-cert-genre rec-elipsis">
		  |        
		  |
		  |            
		  |            
		  |                
		  |                    <span title="Ratings certificate for Madagascar: Escape 2 Africa (2008)"
		  |                          class="us_pg titlePageSprite absmiddle"></span>
		  |
		  |                     Animation          
		  | <span class="ghost">|</span> 
		  |                     Action          
		  | <span class="ghost">|</span> 
		  |                     Adventure          
		  |
		  |        
		  |    </div>
		  |             
		  |             <div class="rec-rating">
		  |               
		  |
		  |  
		  |
		  |  
		  |<div class="rating rating-list" data-starbar-class="rating-list" data-auth="" data-user="" id="tt0479952|imdb|6.7|6.7|p13nsims-title|tt0441773|title|main" data-ga-identifier=""
		  |title="Users rated this 6.7/10 (98,138 votes) - click stars to rate" >
		  |<span class="rating-bg">&nbsp;</span>
		  |<span class="rating-imdb " style="width: 93.8px">&nbsp;</span>
		  |<span class="rating-stars">
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>1</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>2</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>3</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>4</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>5</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>6</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>7</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>8</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>9</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>10</span></a>
		  |</span>
		  |<span class="rating-rating "><span class="value">6.7</span><span class="grey">/</span><span class="grey">10</span></span>
		  |<span class="rating-cancel "><a href="/title/tt0479952/vote?v=X;k=" title="Delete" rel="nofollow"><span>X</span></a></span>
		  |&nbsp;</div>
		  |
		  |               <div class="rec-outline">
		  |    <p>
		  |The animals try to fly back to New York City, but crash-land on an African wildlife refuge, where Alex is reunited with his parents.    </p>
		  |               </div>
		  |               
		  |             </div>  
		  |             
		  |           </div>
		  |           
		  |           <div class="rec-jaw-lower">
		  |             <div class="rec-jaw-teeth"></div>                       
		  | <div class="rec-director rec-ellipsis">
		  |      <b>Directors:</b>
		  |Eric Darnell,
		  |Tom McGrath  </div>
		  |<div class="rec-actor rec-ellipsis"> <span>
		  |    <b>Stars:</b> 
		  |Ben Stiller, 
		  |Chris Rock, 
		  |David Schwimmer</span></div> 
		  |           </div>
		  |           
		  |         </div>
		  |       </div>
		  |
		  |      </div>
		  |     
		  |         
		  |      <div class="rec_overview" data-tconst="tt1080016">
		  |    
		  |    
		  |    
		  |    <div class="rec_poster" data-info="" data-spec="p13nsims:tt0441773" data-tconst="tt1080016">
		  |        <div class="rec_overlay">
		  |            <div class="rec_filter"></div>
		  |            <div class="glyph rec_watchlist_glyph"></div>
		  |            <div class="glyph rec_blocked_glyph"></div>
		  |            <div class="glyph rec_rating_glyph"></div>
		  |            <div class="glyph rec_pending_glyph"></div>                        
		  |        </div>            
		  |<a href="/title/tt1080016/?ref_=tt_rec_tti" ><img height="190" width="128" alt="Ice Age: Dawn of the Dinosaurs" title="Ice Age: Dawn of the Dinosaurs"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/large/film-184890147._V379391879_.png"class="loadlate hidden rec_poster_img" loadlate="http://ia.media-imdb.com/images/M/MV5BMjA4NDI0Mjg4NV5BMl5BanBnXkFtZTcwOTM1NTY0Mg@@._V1_SY190_CR4,0,128,190_AL_.jpg" /> <br/>
		  |</a>    </div>
		  |
		  |    
		  |       <div class="rec_actions">
		  |     
		  |         <div class="rec_action_divider">
		  |           <div class="wlb_classic_wrapper">
		  |             <span class="wlb_wrapper">
		  |               <a class="rec_wlb_watchlist_btn" data-tconst="tt1080016" data-size="medium" data-caller-name="p13nsims-title" data-type="primary"></a>
		  |             </span>
		  |           </div>  
		  |         </div> 
		  |       
		  |         <div class="rec_action_divider">
		  |           <span class="btn2_wrapper">
		  |             <a class="rec_next rec_half_button btn2 medium btn2_text_on" title="Show me the next title" onclick="">
		  |               <span class="btn2_glyph">0</span>
		  |               <span class="btn2_text">Next &raquo;</span>
		  |             </a>
		  |           </span>
		  |         </div>    
		  |         
		  |             <input type="hidden" name="49e6c" value="e341">
		  |       </div>
		  |       
		  |       <div class="rec_details">
		  |         <div class="rec-info">
		  |         
		  |           <div class="rec-jaw-upper">  
		  |
		  |     <div class="rec-title">
		  |       <a href="/title/tt1080016/?ref_=tt_rec_tt" ><b>Ice Age: Dawn of the Dinosaurs</b></a>
		  |            <span class="nobr">(2009)</span>
		  |   </div>  
		  |             
		  |
		  |
		  |    <div class="rec-cert-genre rec-elipsis">
		  |        
		  |
		  |            
		  |            
		  |                
		  |                    <span title="Ratings certificate for Ice Age: Dawn of the Dinosaurs (2009)"
		  |                          class="us_pg titlePageSprite absmiddle"></span>
		  |
		  |                     Animation          
		  | <span class="ghost">|</span> 
		  |                     Action          
		  | <span class="ghost">|</span> 
		  |                     Adventure          
		  |
		  |        
		  |    </div>
		  |             
		  |             <div class="rec-rating">
		  |               
		  |
		  |  
		  |
		  |  
		  |<div class="rating rating-list" data-starbar-class="rating-list" data-auth="" data-user="" id="tt1080016|imdb|7.1|7.1|p13nsims-title|tt0441773|title|main" data-ga-identifier=""
		  |title="Users rated this 7.1/10 (115,168 votes) - click stars to rate" >
		  |<span class="rating-bg">&nbsp;</span>
		  |<span class="rating-imdb " style="width: 99.4px">&nbsp;</span>
		  |<span class="rating-stars">
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>1</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>2</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>3</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>4</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>5</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>6</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>7</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>8</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>9</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>10</span></a>
		  |</span>
		  |<span class="rating-rating "><span class="value">7.1</span><span class="grey">/</span><span class="grey">10</span></span>
		  |<span class="rating-cancel "><a href="/title/tt1080016/vote?v=X;k=" title="Delete" rel="nofollow"><span>X</span></a></span>
		  |&nbsp;</div>
		  |
		  |               <div class="rec-outline">
		  |    <p>
		  |When Sid's attempt to adopt three dinosaur eggs gets him abducted by their real mother to an underground lost world, his friends attempt to rescue him.    </p>
		  |               </div>
		  |               
		  |             </div>  
		  |             
		  |           </div>
		  |           
		  |           <div class="rec-jaw-lower">
		  |             <div class="rec-jaw-teeth"></div>                       
		  | <div class="rec-director rec-ellipsis">
		  |      <b>Directors:</b>
		  |Carlos Saldanha,
		  |Mike Thurmeier  </div>
		  |<div class="rec-actor rec-ellipsis"> <span>
		  |    <b>Stars:</b> 
		  |Ray Romano, 
		  |John Leguizamo, 
		  |Denis Leary</span></div> 
		  |           </div>
		  |           
		  |         </div>
		  |       </div>
		  |
		  |      </div>
		  |     
		  |         
		  |      <div class="rec_overview" data-tconst="tt0892782">
		  |    
		  |    
		  |    
		  |    <div class="rec_poster" data-info="" data-spec="p13nsims:tt0441773" data-tconst="tt0892782">
		  |        <div class="rec_overlay">
		  |            <div class="rec_filter"></div>
		  |            <div class="glyph rec_watchlist_glyph"></div>
		  |            <div class="glyph rec_blocked_glyph"></div>
		  |            <div class="glyph rec_rating_glyph"></div>
		  |            <div class="glyph rec_pending_glyph"></div>                        
		  |        </div>            
		  |<a href="/title/tt0892782/?ref_=tt_rec_tti" ><img height="190" width="128" alt="Monsters vs. Aliens" title="Monsters vs. Aliens"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/large/film-184890147._V379391879_.png"class="loadlate hidden rec_poster_img" loadlate="http://ia.media-imdb.com/images/M/MV5BMTY0OTQ3MzE3MV5BMl5BanBnXkFtZTcwMDQyMzMzMg@@._V1_SY190_CR0,0,128,190_AL_.jpg" /> <br/>
		  |</a>    </div>
		  |
		  |    
		  |       <div class="rec_actions">
		  |     
		  |         <div class="rec_action_divider">
		  |           <div class="wlb_classic_wrapper">
		  |             <span class="wlb_wrapper">
		  |               <a class="rec_wlb_watchlist_btn" data-tconst="tt0892782" data-size="medium" data-caller-name="p13nsims-title" data-type="primary"></a>
		  |             </span>
		  |           </div>  
		  |         </div> 
		  |       
		  |         <div class="rec_action_divider">
		  |           <span class="btn2_wrapper">
		  |             <a class="rec_next rec_half_button btn2 medium btn2_text_on" title="Show me the next title" onclick="">
		  |               <span class="btn2_glyph">0</span>
		  |               <span class="btn2_text">Next &raquo;</span>
		  |             </a>
		  |           </span>
		  |         </div>    
		  |         
		  |             <input type="hidden" name="49e6c" value="e341">
		  |       </div>
		  |       
		  |       <div class="rec_details">
		  |         <div class="rec-info">
		  |         
		  |           <div class="rec-jaw-upper">  
		  |
		  |     <div class="rec-title">
		  |       <a href="/title/tt0892782/?ref_=tt_rec_tt" ><b>Monsters vs. Aliens</b></a>
		  |            <span class="nobr">(2009)</span>
		  |   </div>  
		  |             
		  |
		  |
		  |    <div class="rec-cert-genre rec-elipsis">
		  |        
		  |
		  |            
		  |            
		  |                
		  |                    <span title="Ratings certificate for Monsters vs. Aliens (2009)"
		  |                          class="us_pg titlePageSprite absmiddle"></span>
		  |
		  |                     Animation          
		  | <span class="ghost">|</span> 
		  |                     Action          
		  | <span class="ghost">|</span> 
		  |                     Adventure          
		  |
		  |        
		  |    </div>
		  |             
		  |             <div class="rec-rating">
		  |               
		  |
		  |  
		  |
		  |  
		  |<div class="rating rating-list" data-starbar-class="rating-list" data-auth="" data-user="" id="tt0892782|imdb|6.6|6.6|p13nsims-title|tt0441773|title|main" data-ga-identifier=""
		  |title="Users rated this 6.6/10 (86,429 votes) - click stars to rate" >
		  |<span class="rating-bg">&nbsp;</span>
		  |<span class="rating-imdb " style="width: 92.4px">&nbsp;</span>
		  |<span class="rating-stars">
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>1</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>2</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>3</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>4</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>5</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>6</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>7</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>8</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>9</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>10</span></a>
		  |</span>
		  |<span class="rating-rating "><span class="value">6.6</span><span class="grey">/</span><span class="grey">10</span></span>
		  |<span class="rating-cancel "><a href="/title/tt0892782/vote?v=X;k=" title="Delete" rel="nofollow"><span>X</span></a></span>
		  |&nbsp;</div>
		  |
		  |               <div class="rec-outline">
		  |    <p>
		  |A woman transformed into a giant after she is struck by a meteorite on her wedding day becomes part of a team of monsters sent in by the U.S. government to defeat an alien mastermind trying to take over Earth.    </p>
		  |               </div>
		  |               
		  |             </div>  
		  |             
		  |           </div>
		  |           
		  |           <div class="rec-jaw-lower">
		  |             <div class="rec-jaw-teeth"></div>                       
		  | <div class="rec-director rec-ellipsis">
		  |      <b>Directors:</b>
		  |Rob Letterman,
		  |Conrad Vernon  </div>
		  |<div class="rec-actor rec-ellipsis"> <span>
		  |    <b>Stars:</b> 
		  |Reese Witherspoon, 
		  |Rainn Wilson, 
		  |Stephen Colbert</span></div> 
		  |           </div>
		  |           
		  |         </div>
		  |       </div>
		  |
		  |      </div>
		  |     
		  |         
		  |      <div class="rec_overview" data-tconst="tt1690953">
		  |    
		  |    
		  |    
		  |    <div class="rec_poster" data-info="" data-spec="p13nsims:tt0441773" data-tconst="tt1690953">
		  |        <div class="rec_overlay">
		  |            <div class="rec_filter"></div>
		  |            <div class="glyph rec_watchlist_glyph"></div>
		  |            <div class="glyph rec_blocked_glyph"></div>
		  |            <div class="glyph rec_rating_glyph"></div>
		  |            <div class="glyph rec_pending_glyph"></div>                        
		  |        </div>            
		  |<a href="/title/tt1690953/?ref_=tt_rec_tti" ><img height="190" width="128" alt="Despicable Me 2" title="Despicable Me 2"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/large/film-184890147._V379391879_.png"class="loadlate hidden rec_poster_img" loadlate="http://ia.media-imdb.com/images/M/MV5BMjExNjAyNTcyMF5BMl5BanBnXkFtZTgwODQzMjQ3MDE@._V1_SY190_CR0,0,128,190_AL_.jpg" /> <br/>
		  |</a>    </div>
		  |
		  |    
		  |       <div class="rec_actions">
		  |     
		  |         <div class="rec_action_divider">
		  |           <div class="wlb_classic_wrapper">
		  |             <span class="wlb_wrapper">
		  |               <a class="rec_wlb_watchlist_btn" data-tconst="tt1690953" data-size="medium" data-caller-name="p13nsims-title" data-type="primary"></a>
		  |             </span>
		  |           </div>  
		  |         </div> 
		  |       
		  |         <div class="rec_action_divider">
		  |           <span class="btn2_wrapper">
		  |             <a class="rec_next rec_half_button btn2 medium btn2_text_on" title="Show me the next title" onclick="">
		  |               <span class="btn2_glyph">0</span>
		  |               <span class="btn2_text">Next &raquo;</span>
		  |             </a>
		  |           </span>
		  |         </div>    
		  |         
		  |             <input type="hidden" name="49e6c" value="e341">
		  |       </div>
		  |       
		  |       <div class="rec_details">
		  |         <div class="rec-info">
		  |         
		  |           <div class="rec-jaw-upper">  
		  |
		  |     <div class="rec-title">
		  |       <a href="/title/tt1690953/?ref_=tt_rec_tt" ><b>Despicable Me 2</b></a>
		  |            <span class="nobr">(2013)</span>
		  |   </div>  
		  |             
		  |
		  |
		  |    <div class="rec-cert-genre rec-elipsis">
		  |        
		  |
		  |            
		  |            
		  |                
		  |                    <span title="Ratings certificate for Despicable Me 2 (2013)"
		  |                          class="us_pg titlePageSprite absmiddle"></span>
		  |
		  |                     Animation          
		  | <span class="ghost">|</span> 
		  |                     Adventure          
		  | <span class="ghost">|</span> 
		  |                     Comedy          
		  |
		  |        
		  |    </div>
		  |             
		  |             <div class="rec-rating">
		  |               
		  |
		  |  
		  |
		  |  
		  |<div class="rating rating-list" data-starbar-class="rating-list" data-auth="" data-user="" id="tt1690953|imdb|7.6|7.6|p13nsims-title|tt0441773|title|main" data-ga-identifier=""
		  |title="Users rated this 7.6/10 (177,166 votes) - click stars to rate" >
		  |<span class="rating-bg">&nbsp;</span>
		  |<span class="rating-imdb " style="width: 106.4px">&nbsp;</span>
		  |<span class="rating-stars">
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>1</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>2</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>3</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>4</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>5</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>6</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>7</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>8</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>9</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>10</span></a>
		  |</span>
		  |<span class="rating-rating "><span class="value">7.6</span><span class="grey">/</span><span class="grey">10</span></span>
		  |<span class="rating-cancel "><a href="/title/tt1690953/vote?v=X;k=" title="Delete" rel="nofollow"><span>X</span></a></span>
		  |&nbsp;</div>
		  |
		  |               <div class="rec-outline">
		  |    <p>
		  |Gru is recruited by the Anti-Villain League to help deal with a powerful new super criminal.    </p>
		  |               </div>
		  |               
		  |             </div>  
		  |             
		  |           </div>
		  |           
		  |           <div class="rec-jaw-lower">
		  |             <div class="rec-jaw-teeth"></div>                       
		  | <div class="rec-director rec-ellipsis">
		  |      <b>Directors:</b>
		  |Pierre Coffin,
		  |Chris Renaud  </div>
		  |<div class="rec-actor rec-ellipsis"> <span>
		  |    <b>Stars:</b> 
		  |Steve Carell, 
		  |Kristen Wiig, 
		  |Benjamin Bratt</span></div> 
		  |           </div>
		  |           
		  |         </div>
		  |       </div>
		  |
		  |      </div>
		  |     
		  |         
		  |      <div class="rec_overview" data-tconst="tt0844471">
		  |    
		  |    
		  |    
		  |    <div class="rec_poster" data-info="" data-spec="p13nsims:tt0441773" data-tconst="tt0844471">
		  |        <div class="rec_overlay">
		  |            <div class="rec_filter"></div>
		  |            <div class="glyph rec_watchlist_glyph"></div>
		  |            <div class="glyph rec_blocked_glyph"></div>
		  |            <div class="glyph rec_rating_glyph"></div>
		  |            <div class="glyph rec_pending_glyph"></div>                        
		  |        </div>            
		  |<a href="/title/tt0844471/?ref_=tt_rec_tti" ><img height="190" width="128" alt="Cloudy with a Chance of Meatballs" title="Cloudy with a Chance of Meatballs"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/large/film-184890147._V379391879_.png"class="loadlate hidden rec_poster_img" loadlate="http://ia.media-imdb.com/images/M/MV5BMTg0MjAwNDI5MV5BMl5BanBnXkFtZTcwODkyMzg2Mg@@._V1_SX128_CR0,0,128,190_AL_.jpg" /> <br/>
		  |</a>    </div>
		  |
		  |    
		  |       <div class="rec_actions">
		  |     
		  |         <div class="rec_action_divider">
		  |           <div class="wlb_classic_wrapper">
		  |             <span class="wlb_wrapper">
		  |               <a class="rec_wlb_watchlist_btn" data-tconst="tt0844471" data-size="medium" data-caller-name="p13nsims-title" data-type="primary"></a>
		  |             </span>
		  |           </div>  
		  |         </div> 
		  |       
		  |         <div class="rec_action_divider">
		  |           <span class="btn2_wrapper">
		  |             <a class="rec_next rec_half_button btn2 medium btn2_text_on" title="Show me the next title" onclick="">
		  |               <span class="btn2_glyph">0</span>
		  |               <span class="btn2_text">Next &raquo;</span>
		  |             </a>
		  |           </span>
		  |         </div>    
		  |         
		  |             <input type="hidden" name="49e6c" value="e341">
		  |       </div>
		  |       
		  |       <div class="rec_details">
		  |         <div class="rec-info">
		  |         
		  |           <div class="rec-jaw-upper">  
		  |
		  |     <div class="rec-title">
		  |       <a href="/title/tt0844471/?ref_=tt_rec_tt" ><b>Cloudy with a Chance of Meatballs</b></a>
		  |            <span class="nobr">(2009)</span>
		  |   </div>  
		  |             
		  |
		  |
		  |    <div class="rec-cert-genre rec-elipsis">
		  |        
		  |
		  |            
		  |            
		  |                
		  |                    <span title="Ratings certificate for Cloudy with a Chance of Meatballs (2009)"
		  |                          class="us_pg titlePageSprite absmiddle"></span>
		  |
		  |                     Animation          
		  | <span class="ghost">|</span> 
		  |                     Comedy          
		  | <span class="ghost">|</span> 
		  |                     Family          
		  |
		  |        
		  |    </div>
		  |             
		  |             <div class="rec-rating">
		  |               
		  |
		  |  
		  |
		  |  
		  |<div class="rating rating-list" data-starbar-class="rating-list" data-auth="" data-user="" id="tt0844471|imdb|7|7|p13nsims-title|tt0441773|title|main" data-ga-identifier=""
		  |title="Users rated this 7/10 (105,924 votes) - click stars to rate" >
		  |<span class="rating-bg">&nbsp;</span>
		  |<span class="rating-imdb " style="width: 98px">&nbsp;</span>
		  |<span class="rating-stars">
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>1</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>2</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>3</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>4</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>5</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>6</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>7</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>8</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>9</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>10</span></a>
		  |</span>
		  |<span class="rating-rating "><span class="value">7</span><span class="grey">/</span><span class="grey">10</span></span>
		  |<span class="rating-cancel "><a href="/title/tt0844471/vote?v=X;k=" title="Delete" rel="nofollow"><span>X</span></a></span>
		  |&nbsp;</div>
		  |
		  |               <div class="rec-outline">
		  |    <p>
		  |The most delicious event since macaroni met cheese. Inspired by the beloved children's book, the film focuses on a town where food falls from the sky like rain.    </p>
		  |               </div>
		  |               
		  |             </div>  
		  |             
		  |           </div>
		  |           
		  |           <div class="rec-jaw-lower">
		  |             <div class="rec-jaw-teeth"></div>                       
		  | <div class="rec-director rec-ellipsis">
		  |      <b>Directors:</b>
		  |Phil Lord,
		  |Christopher Miller  </div>
		  |<div class="rec-actor rec-ellipsis"> <span>
		  |    <b>Stars:</b> 
		  |Anna Faris, 
		  |Bill Hader, 
		  |Bruce Campbell</span></div> 
		  |           </div>
		  |           
		  |         </div>
		  |       </div>
		  |
		  |      </div>
		  |     
		  |         
		  |      <div class="rec_overview" data-tconst="tt0120630">
		  |    
		  |    
		  |    
		  |    <div class="rec_poster" data-info="" data-spec="p13nsims:tt0441773" data-tconst="tt0120630">
		  |        <div class="rec_overlay">
		  |            <div class="rec_filter"></div>
		  |            <div class="glyph rec_watchlist_glyph"></div>
		  |            <div class="glyph rec_blocked_glyph"></div>
		  |            <div class="glyph rec_rating_glyph"></div>
		  |            <div class="glyph rec_pending_glyph"></div>                        
		  |        </div>            
		  |<a href="/title/tt0120630/?ref_=tt_rec_tti" ><img height="190" width="128" alt="Chicken Run" title="Chicken Run"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/large/film-184890147._V379391879_.png"class="loadlate hidden rec_poster_img" loadlate="http://ia.media-imdb.com/images/M/MV5BMTk1MjE3MjQ0OF5BMl5BanBnXkFtZTcwMTcyMTcyMQ@@._V1_SY190_CR4,0,128,190_AL_.jpg" /> <br/>
		  |</a>    </div>
		  |
		  |    
		  |       <div class="rec_actions">
		  |     
		  |         <div class="rec_action_divider">
		  |           <div class="wlb_classic_wrapper">
		  |             <span class="wlb_wrapper">
		  |               <a class="rec_wlb_watchlist_btn" data-tconst="tt0120630" data-size="medium" data-caller-name="p13nsims-title" data-type="primary"></a>
		  |             </span>
		  |           </div>  
		  |         </div> 
		  |       
		  |         <div class="rec_action_divider">
		  |           <span class="btn2_wrapper">
		  |             <a class="rec_next rec_half_button btn2 medium btn2_text_on" title="Show me the next title" onclick="">
		  |               <span class="btn2_glyph">0</span>
		  |               <span class="btn2_text">Next &raquo;</span>
		  |             </a>
		  |           </span>
		  |         </div>    
		  |         
		  |             <input type="hidden" name="49e6c" value="e341">
		  |       </div>
		  |       
		  |       <div class="rec_details">
		  |         <div class="rec-info">
		  |         
		  |           <div class="rec-jaw-upper">  
		  |
		  |     <div class="rec-title">
		  |       <a href="/title/tt0120630/?ref_=tt_rec_tt" ><b>Chicken Run</b></a>
		  |            <span class="nobr">(2000)</span>
		  |   </div>  
		  |             
		  |
		  |
		  |    <div class="rec-cert-genre rec-elipsis">
		  |        
		  |
		  |            
		  |            
		  |                
		  |                    <span title="Ratings certificate for Chicken Run (2000)"
		  |                          class="us_g titlePageSprite absmiddle"></span>
		  |
		  |                     Animation          
		  | <span class="ghost">|</span> 
		  |                     Family          
		  | <span class="ghost">|</span> 
		  |                     Comedy          
		  |
		  |        
		  |    </div>
		  |             
		  |             <div class="rec-rating">
		  |               
		  |
		  |  
		  |
		  |  
		  |<div class="rating rating-list" data-starbar-class="rating-list" data-auth="" data-user="" id="tt0120630|imdb|7.1|7.1|p13nsims-title|tt0441773|title|main" data-ga-identifier=""
		  |title="Users rated this 7.1/10 (109,918 votes) - click stars to rate" >
		  |<span class="rating-bg">&nbsp;</span>
		  |<span class="rating-imdb " style="width: 99.4px">&nbsp;</span>
		  |<span class="rating-stars">
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>1</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>2</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>3</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>4</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>5</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>6</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>7</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>8</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>9</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>10</span></a>
		  |</span>
		  |<span class="rating-rating "><span class="value">7.1</span><span class="grey">/</span><span class="grey">10</span></span>
		  |<span class="rating-cancel "><a href="/title/tt0120630/vote?v=X;k=" title="Delete" rel="nofollow"><span>X</span></a></span>
		  |&nbsp;</div>
		  |
		  |               <div class="rec-outline">
		  |    <p>
		  |Chicken Run is a comedy escape drama with a touch of passion set on a sinister Yorks chicken farm in 1950s England.    </p>
		  |               </div>
		  |               
		  |             </div>  
		  |             
		  |           </div>
		  |           
		  |           <div class="rec-jaw-lower">
		  |             <div class="rec-jaw-teeth"></div>                       
		  | <div class="rec-director rec-ellipsis">
		  |      <b>Directors:</b>
		  |Peter Lord,
		  |Nick Park  </div>
		  |<div class="rec-actor rec-ellipsis"> <span>
		  |    <b>Stars:</b> 
		  |Mel Gibson, 
		  |Julia Sawalha, 
		  |Phil Daniels</span></div> 
		  |           </div>
		  |           
		  |         </div>
		  |       </div>
		  |
		  |      </div>
		  |     
		  |         
		  |      <div class="rec_overview" data-tconst="tt0307453">
		  |    
		  |    
		  |    
		  |    <div class="rec_poster" data-info="" data-spec="p13nsims:tt0441773" data-tconst="tt0307453">
		  |        <div class="rec_overlay">
		  |            <div class="rec_filter"></div>
		  |            <div class="glyph rec_watchlist_glyph"></div>
		  |            <div class="glyph rec_blocked_glyph"></div>
		  |            <div class="glyph rec_rating_glyph"></div>
		  |            <div class="glyph rec_pending_glyph"></div>                        
		  |        </div>            
		  |<a href="/title/tt0307453/?ref_=tt_rec_tti" ><img height="190" width="128" alt="Shark Tale" title="Shark Tale"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/large/film-184890147._V379391879_.png"class="loadlate hidden rec_poster_img" loadlate="http://ia.media-imdb.com/images/M/MV5BMTMxMjY0NzE2M15BMl5BanBnXkFtZTcwNTc3ODcyMw@@._V1_SY190_CR0,0,128,190_AL_.jpg" /> <br/>
		  |</a>    </div>
		  |
		  |    
		  |       <div class="rec_actions">
		  |     
		  |         <div class="rec_action_divider">
		  |           <div class="wlb_classic_wrapper">
		  |             <span class="wlb_wrapper">
		  |               <a class="rec_wlb_watchlist_btn" data-tconst="tt0307453" data-size="medium" data-caller-name="p13nsims-title" data-type="primary"></a>
		  |             </span>
		  |           </div>  
		  |         </div> 
		  |       
		  |         <div class="rec_action_divider">
		  |           <span class="btn2_wrapper">
		  |             <a class="rec_next rec_half_button btn2 medium btn2_text_on" title="Show me the next title" onclick="">
		  |               <span class="btn2_glyph">0</span>
		  |               <span class="btn2_text">Next &raquo;</span>
		  |             </a>
		  |           </span>
		  |         </div>    
		  |         
		  |             <input type="hidden" name="49e6c" value="e341">
		  |       </div>
		  |       
		  |       <div class="rec_details">
		  |         <div class="rec-info">
		  |         
		  |           <div class="rec-jaw-upper">  
		  |
		  |     <div class="rec-title">
		  |       <a href="/title/tt0307453/?ref_=tt_rec_tt" ><b>Shark Tale</b></a>
		  |            <span class="nobr">(2004)</span>
		  |   </div>  
		  |             
		  |
		  |
		  |    <div class="rec-cert-genre rec-elipsis">
		  |        
		  |
		  |            
		  |            
		  |                
		  |                    <span title="Ratings certificate for Shark Tale (2004)"
		  |                          class="us_pg titlePageSprite absmiddle"></span>
		  |
		  |                     Animation          
		  | <span class="ghost">|</span> 
		  |                     Adventure          
		  | <span class="ghost">|</span> 
		  |                     Comedy          
		  |
		  |        
		  |    </div>
		  |             
		  |             <div class="rec-rating">
		  |               
		  |
		  |  
		  |
		  |  
		  |<div class="rating rating-list" data-starbar-class="rating-list" data-auth="" data-user="" id="tt0307453|imdb|6|6|p13nsims-title|tt0441773|title|main" data-ga-identifier=""
		  |title="Users rated this 6/10 (104,276 votes) - click stars to rate" >
		  |<span class="rating-bg">&nbsp;</span>
		  |<span class="rating-imdb " style="width: 84px">&nbsp;</span>
		  |<span class="rating-stars">
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>1</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>2</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>3</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>4</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>5</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>6</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>7</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>8</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>9</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>10</span></a>
		  |</span>
		  |<span class="rating-rating "><span class="value">6</span><span class="grey">/</span><span class="grey">10</span></span>
		  |<span class="rating-cancel "><a href="/title/tt0307453/vote?v=X;k=" title="Delete" rel="nofollow"><span>X</span></a></span>
		  |&nbsp;</div>
		  |
		  |               <div class="rec-outline">
		  |    <p>
		  |When a son of a gangster shark boss is accidently killed while on the hunt, his would-be prey and his vegetarian brother both decide to use the incident to their own advantage.    </p>
		  |               </div>
		  |               
		  |             </div>  
		  |             
		  |           </div>
		  |           
		  |           <div class="rec-jaw-lower">
		  |             <div class="rec-jaw-teeth"></div>                       
		  | <div class="rec-director rec-ellipsis">
		  |      <b>Directors:</b>
		  |Bibo Bergeron,
		  |Vicky Jenson, <a href="fullcredits?ref_=tt_ov_dr#directors" >and 1 more credit</a>&nbsp;&raquo;
		  |  </div>
		  |<div class="rec-actor rec-ellipsis"> <span>
		  |    <b>Stars:</b> 
		  |Will Smith, 
		  |Robert De Niro, 
		  |Renée Zellweger</span></div> 
		  |           </div>
		  |           
		  |         </div>
		  |       </div>
		  |
		  |      </div>
		  |     
		  |         
		  |      <div class="rec_overview" data-tconst="tt0837562">
		  |    
		  |    
		  |    
		  |    <div class="rec_poster" data-info="" data-spec="p13nsims:tt0441773" data-tconst="tt0837562">
		  |        <div class="rec_overlay">
		  |            <div class="rec_filter"></div>
		  |            <div class="glyph rec_watchlist_glyph"></div>
		  |            <div class="glyph rec_blocked_glyph"></div>
		  |            <div class="glyph rec_rating_glyph"></div>
		  |            <div class="glyph rec_pending_glyph"></div>                        
		  |        </div>            
		  |<a href="/title/tt0837562/?ref_=tt_rec_tti" ><img height="190" width="128" alt="Hotel Transylvania" title="Hotel Transylvania"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/large/film-184890147._V379391879_.png"class="loadlate hidden rec_poster_img" loadlate="http://ia.media-imdb.com/images/M/MV5BMTM3NjQyODI3M15BMl5BanBnXkFtZTcwMDM4NjM0OA@@._V1_SY190_CR0,0,128,190_AL_.jpg" /> <br/>
		  |</a>    </div>
		  |
		  |    
		  |       <div class="rec_actions">
		  |     
		  |         <div class="rec_action_divider">
		  |           <div class="wlb_classic_wrapper">
		  |             <span class="wlb_wrapper">
		  |               <a class="rec_wlb_watchlist_btn" data-tconst="tt0837562" data-size="medium" data-caller-name="p13nsims-title" data-type="primary"></a>
		  |             </span>
		  |           </div>  
		  |         </div> 
		  |       
		  |         <div class="rec_action_divider">
		  |           <span class="btn2_wrapper">
		  |             <a class="rec_next rec_half_button btn2 medium btn2_text_on" title="Show me the next title" onclick="">
		  |               <span class="btn2_glyph">0</span>
		  |               <span class="btn2_text">Next &raquo;</span>
		  |             </a>
		  |           </span>
		  |         </div>    
		  |         
		  |             <input type="hidden" name="49e6c" value="e341">
		  |       </div>
		  |       
		  |       <div class="rec_details">
		  |         <div class="rec-info">
		  |         
		  |           <div class="rec-jaw-upper">  
		  |
		  |     <div class="rec-title">
		  |       <a href="/title/tt0837562/?ref_=tt_rec_tt" ><b>Hotel Transylvania</b></a>
		  |            <span class="nobr">(2012)</span>
		  |   </div>  
		  |             
		  |
		  |
		  |    <div class="rec-cert-genre rec-elipsis">
		  |        
		  |
		  |            
		  |            
		  |                
		  |                    <span title="Ratings certificate for Hotel Transylvania (2012)"
		  |                          class="us_pg titlePageSprite absmiddle"></span>
		  |
		  |                     Animation          
		  | <span class="ghost">|</span> 
		  |                     Comedy          
		  | <span class="ghost">|</span> 
		  |                     Family          
		  |
		  |        
		  |    </div>
		  |             
		  |             <div class="rec-rating">
		  |               
		  |
		  |  
		  |
		  |  
		  |<div class="rating rating-list" data-starbar-class="rating-list" data-auth="" data-user="" id="tt0837562|imdb|7.1|7.1|p13nsims-title|tt0441773|title|main" data-ga-identifier=""
		  |title="Users rated this 7.1/10 (102,475 votes) - click stars to rate" >
		  |<span class="rating-bg">&nbsp;</span>
		  |<span class="rating-imdb " style="width: 99.4px">&nbsp;</span>
		  |<span class="rating-stars">
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>1</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>2</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>3</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>4</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>5</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>6</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>7</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>8</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>9</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>10</span></a>
		  |</span>
		  |<span class="rating-rating "><span class="value">7.1</span><span class="grey">/</span><span class="grey">10</span></span>
		  |<span class="rating-cancel "><a href="/title/tt0837562/vote?v=X;k=" title="Delete" rel="nofollow"><span>X</span></a></span>
		  |&nbsp;</div>
		  |
		  |               <div class="rec-outline">
		  |    <p>
		  |Dracula, who operates a high-end resort away from the human world, goes into overprotective mode when a boy discovers the resort and falls for the count's teen-aged daughter.    </p>
		  |               </div>
		  |               
		  |             </div>  
		  |             
		  |           </div>
		  |           
		  |           <div class="rec-jaw-lower">
		  |             <div class="rec-jaw-teeth"></div>                       
		  | <div class="rec-director rec-ellipsis">
		  |      <b>Director:</b>
		  |Genndy Tartakovsky  </div>
		  |<div class="rec-actor rec-ellipsis"> <span>
		  |    <b>Stars:</b> 
		  |Adam Sandler, 
		  |Kevin James, 
		  |Andy Samberg</span></div> 
		  |           </div>
		  |           
		  |         </div>
		  |       </div>
		  |
		  |      </div>
		  |     
		  |         
		  |      <div class="rec_overview" data-tconst="tt0119715">
		  |    
		  |    
		  |    
		  |    <div class="rec_poster" data-info="" data-spec="p13nsims:tt0441773" data-tconst="tt0119715">
		  |        <div class="rec_overlay">
		  |            <div class="rec_filter"></div>
		  |            <div class="glyph rec_watchlist_glyph"></div>
		  |            <div class="glyph rec_blocked_glyph"></div>
		  |            <div class="glyph rec_rating_glyph"></div>
		  |            <div class="glyph rec_pending_glyph"></div>                        
		  |        </div>            
		  |<a href="/title/tt0119715/?ref_=tt_rec_tti" ><img height="190" width="128" alt="Mousehunt" title="Mousehunt"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/large/film-184890147._V379391879_.png"class="loadlate hidden rec_poster_img" loadlate="http://ia.media-imdb.com/images/M/MV5BMTk1MDExMTA5NF5BMl5BanBnXkFtZTcwOTMzNTIyMQ@@._V1_SY190_CR2,0,128,190_AL_.jpg" /> <br/>
		  |</a>    </div>
		  |
		  |    
		  |       <div class="rec_actions">
		  |     
		  |         <div class="rec_action_divider">
		  |           <div class="wlb_classic_wrapper">
		  |             <span class="wlb_wrapper">
		  |               <a class="rec_wlb_watchlist_btn" data-tconst="tt0119715" data-size="medium" data-caller-name="p13nsims-title" data-type="primary"></a>
		  |             </span>
		  |           </div>  
		  |         </div> 
		  |       
		  |         <div class="rec_action_divider">
		  |           <span class="btn2_wrapper">
		  |             <a class="rec_next rec_half_button btn2 medium btn2_text_on" title="Show me the next title" onclick="">
		  |               <span class="btn2_glyph">0</span>
		  |               <span class="btn2_text">Next &raquo;</span>
		  |             </a>
		  |           </span>
		  |         </div>    
		  |         
		  |             <input type="hidden" name="49e6c" value="e341">
		  |       </div>
		  |       
		  |       <div class="rec_details">
		  |         <div class="rec-info">
		  |         
		  |           <div class="rec-jaw-upper">  
		  |
		  |     <div class="rec-title">
		  |       <a href="/title/tt0119715/?ref_=tt_rec_tt" ><b>Mousehunt</b></a>
		  |            <span class="nobr">(1997)</span>
		  |   </div>  
		  |             
		  |
		  |
		  |    <div class="rec-cert-genre rec-elipsis">
		  |        
		  |
		  |            
		  |            
		  |                
		  |                    <span title="Ratings certificate for Mousehunt (1997)"
		  |                          class="us_pg titlePageSprite absmiddle"></span>
		  |
		  |                     Comedy          
		  | <span class="ghost">|</span> 
		  |                     Family          
		  |
		  |
		  |        
		  |    </div>
		  |             
		  |             <div class="rec-rating">
		  |               
		  |
		  |  
		  |
		  |  
		  |<div class="rating rating-list" data-starbar-class="rating-list" data-auth="" data-user="" id="tt0119715|imdb|6.3|6.3|p13nsims-title|tt0441773|title|main" data-ga-identifier=""
		  |title="Users rated this 6.3/10 (35,027 votes) - click stars to rate" >
		  |<span class="rating-bg">&nbsp;</span>
		  |<span class="rating-imdb " style="width: 88.2px">&nbsp;</span>
		  |<span class="rating-stars">
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>1</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>2</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>3</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>4</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>5</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>6</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>7</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>8</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>9</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>10</span></a>
		  |</span>
		  |<span class="rating-rating "><span class="value">6.3</span><span class="grey">/</span><span class="grey">10</span></span>
		  |<span class="rating-cancel "><a href="/title/tt0119715/vote?v=X;k=" title="Delete" rel="nofollow"><span>X</span></a></span>
		  |&nbsp;</div>
		  |
		  |               <div class="rec-outline">
		  |    <p>
		  |Two stumblebum inheritors are determined to rid their antique house of a mouse who is equally determined to stay where he is.    </p>
		  |               </div>
		  |               
		  |             </div>  
		  |             
		  |           </div>
		  |           
		  |           <div class="rec-jaw-lower">
		  |             <div class="rec-jaw-teeth"></div>                       
		  | <div class="rec-director rec-ellipsis">
		  |      <b>Director:</b>
		  |Gore Verbinski  </div>
		  |<div class="rec-actor rec-ellipsis"> <span>
		  |    <b>Stars:</b> 
		  |Nathan Lane, 
		  |Lee Evans, 
		  |Vicki Lewis</span></div> 
		  |           </div>
		  |           
		  |         </div>
		  |       </div>
		  |
		  |      </div>
		  |     
		  |         
		  |      <div class="rec_overview" data-tconst="tt1323594">
		  |    
		  |    
		  |    
		  |    <div class="rec_poster" data-info="" data-spec="p13nsims:tt0441773" data-tconst="tt1323594">
		  |        <div class="rec_overlay">
		  |            <div class="rec_filter"></div>
		  |            <div class="glyph rec_watchlist_glyph"></div>
		  |            <div class="glyph rec_blocked_glyph"></div>
		  |            <div class="glyph rec_rating_glyph"></div>
		  |            <div class="glyph rec_pending_glyph"></div>                        
		  |        </div>            
		  |<a href="/title/tt1323594/?ref_=tt_rec_tti" ><img height="190" width="128" alt="Despicable Me" title="Despicable Me"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/large/film-184890147._V379391879_.png"class="loadlate hidden rec_poster_img" loadlate="http://ia.media-imdb.com/images/M/MV5BMTY3NjY0MTQ0Nl5BMl5BanBnXkFtZTcwMzQ2MTc0Mw@@._V1_SY190_CR0,0,128,190_AL_.jpg" /> <br/>
		  |</a>    </div>
		  |
		  |    
		  |       <div class="rec_actions">
		  |     
		  |         <div class="rec_action_divider">
		  |           <div class="wlb_classic_wrapper">
		  |             <span class="wlb_wrapper">
		  |               <a class="rec_wlb_watchlist_btn" data-tconst="tt1323594" data-size="medium" data-caller-name="p13nsims-title" data-type="primary"></a>
		  |             </span>
		  |           </div>  
		  |         </div> 
		  |       
		  |         <div class="rec_action_divider">
		  |           <span class="btn2_wrapper">
		  |             <a class="rec_next rec_half_button btn2 medium btn2_text_on" title="Show me the next title" onclick="">
		  |               <span class="btn2_glyph">0</span>
		  |               <span class="btn2_text">Next &raquo;</span>
		  |             </a>
		  |           </span>
		  |         </div>    
		  |         
		  |             <input type="hidden" name="49e6c" value="e341">
		  |       </div>
		  |       
		  |       <div class="rec_details">
		  |         <div class="rec-info">
		  |         
		  |           <div class="rec-jaw-upper">  
		  |
		  |     <div class="rec-title">
		  |       <a href="/title/tt1323594/?ref_=tt_rec_tt" ><b>Despicable Me</b></a>
		  |            <span class="nobr">(2010)</span>
		  |   </div>  
		  |             
		  |
		  |
		  |    <div class="rec-cert-genre rec-elipsis">
		  |        
		  |
		  |            
		  |            
		  |                
		  |                    <span title="Ratings certificate for Despicable Me (2010)"
		  |                          class="us_pg titlePageSprite absmiddle"></span>
		  |
		  |                     Animation          
		  | <span class="ghost">|</span> 
		  |                     Comedy          
		  | <span class="ghost">|</span> 
		  |                     Crime          
		  |
		  |        
		  |    </div>
		  |             
		  |             <div class="rec-rating">
		  |               
		  |
		  |  
		  |
		  |  
		  |<div class="rating rating-list" data-starbar-class="rating-list" data-auth="" data-user="" id="tt1323594|imdb|7.7|7.7|p13nsims-title|tt0441773|title|main" data-ga-identifier=""
		  |title="Users rated this 7.7/10 (251,456 votes) - click stars to rate" >
		  |<span class="rating-bg">&nbsp;</span>
		  |<span class="rating-imdb " style="width: 107.8px">&nbsp;</span>
		  |<span class="rating-stars">
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>1</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>2</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>3</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>4</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>5</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>6</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>7</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>8</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>9</span></a>
		  |      <a rel="nofollow" href="/register/login?why=vote&ref_=tt_ov_rt" title="Register or login to rate this title" ><span>10</span></a>
		  |</span>
		  |<span class="rating-rating "><span class="value">7.7</span><span class="grey">/</span><span class="grey">10</span></span>
		  |<span class="rating-cancel "><a href="/title/tt1323594/vote?v=X;k=" title="Delete" rel="nofollow"><span>X</span></a></span>
		  |&nbsp;</div>
		  |
		  |               <div class="rec-outline">
		  |    <p>
		  |When a criminal mastermind uses a trio of orphan girls as pawns for a grand scheme, he finds their love is profoundly changing him for the better.    </p>
		  |               </div>
		  |               
		  |             </div>  
		  |             
		  |           </div>
		  |           
		  |           <div class="rec-jaw-lower">
		  |             <div class="rec-jaw-teeth"></div>                       
		  | <div class="rec-director rec-ellipsis">
		  |      <b>Directors:</b>
		  |Pierre Coffin,
		  |Chris Renaud  </div>
		  |<div class="rec-actor rec-ellipsis"> <span>
		  |    <b>Stars:</b> 
		  |Steve Carell, 
		  |Jason Segel, 
		  |Russell Brand</span></div> 
		  |           </div>
		  |           
		  |         </div>
		  |       </div>
		  |
		  |      </div>
		  |     
		  |    
		  |   </div>
		  |   
		  |                
		  |            </div>
		  |        </div>  
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_TitleRecsWidget_finished');
		  |    }
		  |  </script>
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_TitleCastWidget_started');
		  |    }
		  |  </script>
		  |    <div class="article" id="titleCast">
		  |    <span class=rightcornerlink >
		  |            <a href="/register/login?why=edit&ref_=tt_cl" rel="login">Edit</a>
		  |    </span>
		  |        <h2>Cast</h2>
		  |        
		  |        <table class="cast_list">    
		  |  <tr><td colspan="4" class="castlist_label">Cast overview, first billed only:</td></tr>
		  |      <tr class="odd">
		  |          <td class="primary_photo">
		  |<a href="/name/nm0085312/?ref_=tt_cl_i1" ><img height="44" width="32" alt="Jack Black" title="Jack Black"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/32x44/name-2138558783._V379389446_.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BMTM0ODg1Mjg3Nl5BMl5BanBnXkFtZTcwMDgxNTQwMw@@._V1_SY44_CR0,0,32,44_AL_.jpg" /></a>          </td>
		  |          <td class="itemprop" itemprop="actor" itemscope itemtype="http://schema.org/Person">
		  |<a href="/name/nm0085312/?ref_=tt_cl_t1" itemprop='url'> <span class="itemprop" itemprop="name">Jack Black</span>
		  |</a>          </td>
		  |          <td class="ellipsis">
		  |              ...
		  |          </td>
		  |          <td class="character">
		  |              <div>
		  |            <a href="/character/ch0039061/?ref_=tt_cl_t1" >Po</a> 
		  |  
		  |  
		  |  (voice)
		  |  
		  |                  
		  |              </div>
		  |          </td>
		  |      </tr>
		  |      <tr class="even">
		  |          <td class="primary_photo">
		  |<a href="/name/nm0000163/?ref_=tt_cl_i2" ><img height="44" width="32" alt="Dustin Hoffman" title="Dustin Hoffman"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/32x44/name-2138558783._V379389446_.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BMTc3NzU0ODczMF5BMl5BanBnXkFtZTcwODEyMDY5Mg@@._V1_SY44_CR0,0,32,44_AL_.jpg" /></a>          </td>
		  |          <td class="itemprop" itemprop="actor" itemscope itemtype="http://schema.org/Person">
		  |<a href="/name/nm0000163/?ref_=tt_cl_t2" itemprop='url'> <span class="itemprop" itemprop="name">Dustin Hoffman</span>
		  |</a>          </td>
		  |          <td class="ellipsis">
		  |              ...
		  |          </td>
		  |          <td class="character">
		  |              <div>
		  |            <a href="/character/ch0039064/?ref_=tt_cl_t2" >Shifu</a> 
		  |  
		  |  
		  |  (voice)
		  |  
		  |                  
		  |              </div>
		  |          </td>
		  |      </tr>
		  |      <tr class="odd">
		  |          <td class="primary_photo">
		  |<a href="/name/nm0001401/?ref_=tt_cl_i3" ><img height="44" width="32" alt="Angelina Jolie" title="Angelina Jolie"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/32x44/name-2138558783._V379389446_.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BODg3MzYwMjE4N15BMl5BanBnXkFtZTcwMjU5NzAzNw@@._V1_SY44_CR2,0,32,44_AL_.jpg" /></a>          </td>
		  |          <td class="itemprop" itemprop="actor" itemscope itemtype="http://schema.org/Person">
		  |<a href="/name/nm0001401/?ref_=tt_cl_t3" itemprop='url'> <span class="itemprop" itemprop="name">Angelina Jolie</span>
		  |</a>          </td>
		  |          <td class="ellipsis">
		  |              ...
		  |          </td>
		  |          <td class="character">
		  |              <div>
		  |            <a href="/character/ch0083726/?ref_=tt_cl_t3" >Tigress</a> 
		  |  
		  |  
		  |  (voice)
		  |  
		  |                  
		  |              </div>
		  |          </td>
		  |      </tr>
		  |      <tr class="even">
		  |          <td class="primary_photo">
		  |<a href="/name/nm0574534/?ref_=tt_cl_i4" ><img height="44" width="32" alt="Ian McShane" title="Ian McShane"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/32x44/name-2138558783._V379389446_.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BMTQwMzAzMTIyMF5BMl5BanBnXkFtZTcwMjM1MDM2OQ@@._V1_SY44_CR3,0,32,44_AL_.jpg" /></a>          </td>
		  |          <td class="itemprop" itemprop="actor" itemscope itemtype="http://schema.org/Person">
		  |<a href="/name/nm0574534/?ref_=tt_cl_t4" itemprop='url'> <span class="itemprop" itemprop="name">Ian McShane</span>
		  |</a>          </td>
		  |          <td class="ellipsis">
		  |              ...
		  |          </td>
		  |          <td class="character">
		  |              <div>
		  |            <a href="/character/ch0039066/?ref_=tt_cl_t4" >Tai Lung</a> 
		  |  
		  |  
		  |  (voice)
		  |  
		  |                  
		  |              </div>
		  |          </td>
		  |      </tr>
		  |      <tr class="odd">
		  |          <td class="primary_photo">
		  |<a href="/name/nm0000329/?ref_=tt_cl_i5" ><img height="44" width="32" alt="Jackie Chan" title="Jackie Chan"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/32x44/name-2138558783._V379389446_.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BMTk4MDM0MDUzM15BMl5BanBnXkFtZTcwOTI4MzU1Mw@@._V1_SX32_CR0,0,32,44_AL_.jpg" /></a>          </td>
		  |          <td class="itemprop" itemprop="actor" itemscope itemtype="http://schema.org/Person">
		  |<a href="/name/nm0000329/?ref_=tt_cl_t5" itemprop='url'> <span class="itemprop" itemprop="name">Jackie Chan</span>
		  |</a>          </td>
		  |          <td class="ellipsis">
		  |              ...
		  |          </td>
		  |          <td class="character">
		  |              <div>
		  |            <a href="/character/ch0083728/?ref_=tt_cl_t5" >Monkey</a> 
		  |  
		  |  
		  |  (voice)
		  |  
		  |                  
		  |              </div>
		  |          </td>
		  |      </tr>
		  |      <tr class="even">
		  |          <td class="primary_photo">
		  |<a href="/name/nm0736622/?ref_=tt_cl_i6" ><img height="44" width="32" alt="Seth Rogen" title="Seth Rogen"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/32x44/name-2138558783._V379389446_.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BMjA0ODg0MjI3M15BMl5BanBnXkFtZTcwMzE5NjI3Mg@@._V1_SX32_CR0,0,32,44_AL_.jpg" /></a>          </td>
		  |          <td class="itemprop" itemprop="actor" itemscope itemtype="http://schema.org/Person">
		  |<a href="/name/nm0736622/?ref_=tt_cl_t6" itemprop='url'> <span class="itemprop" itemprop="name">Seth Rogen</span>
		  |</a>          </td>
		  |          <td class="ellipsis">
		  |              ...
		  |          </td>
		  |          <td class="character">
		  |              <div>
		  |            <a href="/character/ch0039068/?ref_=tt_cl_t6" >Mantis</a> 
		  |  
		  |  
		  |  (voice)
		  |  
		  |                  
		  |              </div>
		  |          </td>
		  |      </tr>
		  |      <tr class="odd">
		  |          <td class="primary_photo">
		  |<a href="/name/nm0005154/?ref_=tt_cl_i7" ><img height="44" width="32" alt="Lucy Liu" title="Lucy Liu"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/32x44/name-2138558783._V379389446_.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BOTgxODE0MjI5Ml5BMl5BanBnXkFtZTcwMjkxMzMzMg@@._V1_SX32_CR0,0,32,44_AL_.jpg" /></a>          </td>
		  |          <td class="itemprop" itemprop="actor" itemscope itemtype="http://schema.org/Person">
		  |<a href="/name/nm0005154/?ref_=tt_cl_t7" itemprop='url'> <span class="itemprop" itemprop="name">Lucy Liu</span>
		  |</a>          </td>
		  |          <td class="ellipsis">
		  |              ...
		  |          </td>
		  |          <td class="character">
		  |              <div>
		  |            <a href="/character/ch0039065/?ref_=tt_cl_t7" >Viper</a> 
		  |  
		  |  
		  |  (voice)
		  |  
		  |                  
		  |              </div>
		  |          </td>
		  |      </tr>
		  |      <tr class="even">
		  |          <td class="primary_photo">
		  |<a href="/name/nm0189144/?ref_=tt_cl_i8" ><img height="44" width="32" alt="David Cross" title="David Cross"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/32x44/name-2138558783._V379389446_.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BMTMzMzgwOTI1N15BMl5BanBnXkFtZTcwNTg5ODk2Mw@@._V1_SY44_CR0,0,32,44_AL_.jpg" /></a>          </td>
		  |          <td class="itemprop" itemprop="actor" itemscope itemtype="http://schema.org/Person">
		  |<a href="/name/nm0189144/?ref_=tt_cl_t8" itemprop='url'> <span class="itemprop" itemprop="name">David Cross</span>
		  |</a>          </td>
		  |          <td class="ellipsis">
		  |              ...
		  |          </td>
		  |          <td class="character">
		  |              <div>
		  |            <a href="/character/ch0039071/?ref_=tt_cl_t8" >Crane</a> 
		  |  
		  |  
		  |  (voice)
		  |  
		  |                  
		  |              </div>
		  |          </td>
		  |      </tr>
		  |      <tr class="odd">
		  |          <td class="primary_photo">
		  |<a href="/name/nm0453641/?ref_=tt_cl_i9" ><img height="44" width="32" alt="Randall Duk Kim" title="Randall Duk Kim"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/32x44/name-2138558783._V379389446_.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BMTIwMjY3ODY5M15BMl5BanBnXkFyZXN1bWU@._V1_SY44_CR11,0,32,44_AL_.jpg" /></a>          </td>
		  |          <td class="itemprop" itemprop="actor" itemscope itemtype="http://schema.org/Person">
		  |<a href="/name/nm0453641/?ref_=tt_cl_t9" itemprop='url'> <span class="itemprop" itemprop="name">Randall Duk Kim</span>
		  |</a>          </td>
		  |          <td class="ellipsis">
		  |              ...
		  |          </td>
		  |          <td class="character">
		  |              <div>
		  |            <a href="/character/ch0085860/?ref_=tt_cl_t9" >Oogway</a> 
		  |  
		  |  
		  |  (voice)
		  |  
		  |                  
		  |              </div>
		  |          </td>
		  |      </tr>
		  |      <tr class="even">
		  |          <td class="primary_photo">
		  |<a href="/name/nm0393222/?ref_=tt_cl_i10" ><img height="44" width="32" alt="James Hong" title="James Hong"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/32x44/name-2138558783._V379389446_.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BMTMwMTI2MDMxNV5BMl5BanBnXkFtZTcwNTQxMDg5MQ@@._V1_SY44_CR2,0,32,44_AL_.jpg" /></a>          </td>
		  |          <td class="itemprop" itemprop="actor" itemscope itemtype="http://schema.org/Person">
		  |<a href="/name/nm0393222/?ref_=tt_cl_t10" itemprop='url'> <span class="itemprop" itemprop="name">James Hong</span>
		  |</a>          </td>
		  |          <td class="ellipsis">
		  |              ...
		  |          </td>
		  |          <td class="character">
		  |              <div>
		  |            <a href="/character/ch0089411/?ref_=tt_cl_t10" >Mr. Ping</a> 
		  |  
		  |  
		  |  (voice)
		  |  
		  |                  
		  |              </div>
		  |          </td>
		  |      </tr>
		  |      <tr class="odd">
		  |          <td class="primary_photo">
		  |<a href="/name/nm0283945/?ref_=tt_cl_i11" ><img height="44" width="32" alt="Dan Fogler" title="Dan Fogler"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/32x44/name-2138558783._V379389446_.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BMTQ4MDE1NTc4Ml5BMl5BanBnXkFtZTcwODYxMjgyNw@@._V1_SX32_CR0,0,32,44_AL_.jpg" /></a>          </td>
		  |          <td class="itemprop" itemprop="actor" itemscope itemtype="http://schema.org/Person">
		  |<a href="/name/nm0283945/?ref_=tt_cl_t11" itemprop='url'> <span class="itemprop" itemprop="name">Dan Fogler</span>
		  |</a>          </td>
		  |          <td class="ellipsis">
		  |              ...
		  |          </td>
		  |          <td class="character">
		  |              <div>
		  |            <a href="/character/ch0089412/?ref_=tt_cl_t11" >Zeng</a> 
		  |  
		  |  
		  |  (voice)
		  |  
		  |                  
		  |              </div>
		  |          </td>
		  |      </tr>
		  |      <tr class="even">
		  |          <td class="primary_photo">
		  |<a href="/name/nm0003817/?ref_=tt_cl_i12" ><img height="44" width="32" alt="Michael Clarke Duncan" title="Michael Clarke Duncan"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/32x44/name-2138558783._V379389446_.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BMTI3NDY2ODk5OV5BMl5BanBnXkFtZTYwMjQ0NzE0._V1_SY44_CR2,0,32,44_AL_.jpg" /></a>          </td>
		  |          <td class="itemprop" itemprop="actor" itemscope itemtype="http://schema.org/Person">
		  |<a href="/name/nm0003817/?ref_=tt_cl_t12" itemprop='url'> <span class="itemprop" itemprop="name">Michael Clarke Duncan</span>
		  |</a>          </td>
		  |          <td class="ellipsis">
		  |              ...
		  |          </td>
		  |          <td class="character">
		  |              <div>
		  |            <a href="/character/ch0039069/?ref_=tt_cl_t12" >Commander Vachir</a> 
		  |  
		  |  
		  |  (voice)
		  |  
		  |                  
		  |              </div>
		  |          </td>
		  |      </tr>
		  |      <tr class="odd">
		  |          <td class="primary_photo">
		  |<a href="/name/nm0001431/?ref_=tt_cl_i13" ><img height="44" width="32" alt="Wayne Knight" title="Wayne Knight"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/32x44/name-2138558783._V379389446_.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BMTA1Mjc4MTI5ODReQTJeQWpwZ15BbWU3MDM0NzYyMDc@._V1_SY44_CR0,0,32,44_AL_.jpg" /></a>          </td>
		  |          <td class="itemprop" itemprop="actor" itemscope itemtype="http://schema.org/Person">
		  |<a href="/name/nm0001431/?ref_=tt_cl_t13" itemprop='url'> <span class="itemprop" itemprop="name">Wayne Knight</span>
		  |</a>          </td>
		  |          <td class="ellipsis">
		  |              ...
		  |          </td>
		  |          <td class="character">
		  |              <div>
		  |            <a href="/character/ch0089413/?ref_=tt_cl_t13" >Gang Boss</a> 
		  |  
		  |  
		  |  (voice)
		  |  
		  |                  
		  |              </div>
		  |          </td>
		  |      </tr>
		  |      <tr class="even">
		  |          <td class="primary_photo">
		  |<a href="/name/nm0309307/?ref_=tt_cl_i14" ><img height="44" width="32" alt="Kyle Gass" title="Kyle Gass"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/32x44/name-2138558783._V379389446_.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BNDMxMjk5OTQ2Nl5BMl5BanBnXkFtZTYwODU3NTA2._V1_SX32_CR0,0,32,44_AL_.jpg" /></a>          </td>
		  |          <td class="itemprop" itemprop="actor" itemscope itemtype="http://schema.org/Person">
		  |<a href="/name/nm0309307/?ref_=tt_cl_t14" itemprop='url'> <span class="itemprop" itemprop="name">Kyle Gass</span>
		  |</a>          </td>
		  |          <td class="ellipsis">
		  |              ...
		  |          </td>
		  |          <td class="character">
		  |              <div>
		  |            <a href="/character/ch0014496/?ref_=tt_cl_t14" >KG Shaw</a> 
		  |  
		  |  
		  |  (voice)
		  |  
		  |                  
		  |              </div>
		  |          </td>
		  |      </tr>
		  |      <tr class="odd">
		  |          <td class="primary_photo">
		  |<a href="/name/nm0715469/?ref_=tt_cl_i15" ><img height="44" width="32" alt="JR Reed" title="JR Reed"src="http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/32x44/name-2138558783._V379389446_.png"class="loadlate hidden " loadlate="http://ia.media-imdb.com/images/M/MV5BMTI3MTMwOTgwNV5BMl5BanBnXkFtZTcwODY5MzY1MQ@@._V1_SY44_CR11,0,32,44_AL_.jpg" /></a>          </td>
		  |          <td class="itemprop" itemprop="actor" itemscope itemtype="http://schema.org/Person">
		  |<a href="/name/nm0715469/?ref_=tt_cl_t15" itemprop='url'> <span class="itemprop" itemprop="name">JR Reed</span>
		  |</a>          </td>
		  |          <td class="ellipsis">
		  |              ...
		  |          </td>
		  |          <td class="character">
		  |              <div>
		  |            <a href="/character/ch0089415/?ref_=tt_cl_t15" >JR Shaw</a> 
		  |  
		  |  
		  |  (voice)
		  |  
		  |                  
		  |              </div>
		  |          </td>
		  |      </tr>
		  |        </table>
		  |        <div class="see-more">
		  |            <a href="fullcredits?ref_=tt_cl_sm#cast" >See full cast</a>&nbsp;&raquo;
		  |        </div>
		  |    </div>
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_TitleCastWidget_finished');
		  |    }
		  |  </script>
		  |        
		  |	
		  |	<!-- no content received for slot: maindetails_center_ad -->
		  |	
		  |
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_TitleStorylineWidget_started');
		  |    }
		  |  </script>
		  |    <div class = "article" id="titleStoryLine">
		  |    <span class=rightcornerlink >
		  |            <a href="/register/login?why=edit&ref_=tt_stry" rel="login">Edit</a>
		  |    </span>
		  |    
		  |        <h2>Storyline</h2>
		  |        
		  |        <div class="inline canwrap" itemprop="description">
		  |            <p>
		  |It's the story about a lazy, irreverent slacker panda, named Po, who is the biggest fan of Kung Fu around...which doesn't exactly come in handy while working every day in his family's noodle shop. Unexpectedly chosen to fulfill an ancient prophecy, Po's dreams become reality when he joins the world of Kung Fu and studies alongside his idols, the legendary Furious Five -- Tigress, Crane, Mantis, Viper and Monkey -- under the leadership of their guru, Master Shifu. But before they know it, the vengeful and treacherous snow leopard Tai Lung is headed their way, and it's up to Po to defend everyone from the oncoming threat. Can he turn his dreams of becoming a Kung Fu master into reality? Po puts his heart - and his girth - into the task, and the unlikely hero ultimately finds that his greatest weaknesses turn out to be his greatest strengths.                <em class="nobr">Written by
		  |<a href="/search/title?plot_author=Anthony Pereyra {hypersonic91@yahoo.com}&view=simple&sort=alpha&ref_=tt_stry_pl" >Anthony Pereyra {hypersonic91@yahoo.com}</a></em>            </p>
		  |        </div>
		  |        
		  |        <span class="see-more inline"> 
		  |                <a href="/title/tt0441773/plotsummary?ref_=tt_stry_pl" >Plot Summary</a>
		  |    <span>|</span>
		  |        <a href="/title/tt0441773/synopsis?ref_=tt_stry_pl" >Plot Synopsis</a>
		  |    </span>
		  |        <hr />
		  |        <div class="see-more inline canwrap" itemprop="keywords">
		  |            <h4 class="inline">Plot Keywords:</h4>
		  |<a href="/keyword/kung-fu?ref_=tt_stry_kw" > <span class="itemprop" itemprop="keywords">kung fu</span></a>
		  |                    <span>|</span>
		  |<a href="/keyword/master?ref_=tt_stry_kw" > <span class="itemprop" itemprop="keywords">master</span></a>
		  |                    <span>|</span>
		  |<a href="/keyword/hero?ref_=tt_stry_kw" > <span class="itemprop" itemprop="keywords">hero</span></a>
		  |                    <span>|</span>
		  |<a href="/keyword/panda?ref_=tt_stry_kw" > <span class="itemprop" itemprop="keywords">panda</span></a>
		  |                    <span>|</span>
		  |<a href="/keyword/kung-fu-master?ref_=tt_stry_kw" > <span class="itemprop" itemprop="keywords">kung fu master</span></a>
		  |                                            <span>|</span>&nbsp;<nobr><a href="/title/tt0441773/keywords?ref_=tt_stry_kw" >See more</a>&nbsp;&raquo;</nobr>
		  |        </div>      
		  |        <hr />
		  |        <div class="txt-block">
		  |            <h4 class="inline">Taglines:</h4>
		  |Summertime is Pandatime.                <span class="see-more inline">
		  |<a href="/title/tt0441773/taglines?ref_=tt_stry_tg" > See more</a>&nbsp;&raquo;
		  |                </span>
		  |        </div>
		  |        <hr />
		  |        <div class="see-more inline canwrap" itemprop="genre">
		  |            <h4 class="inline">Genres:</h4>
		  |<a href="/genre/Animation?ref_=tt_stry_gnr" > Animation</a>&nbsp;<span>|</span>
		  |<a href="/genre/Action?ref_=tt_stry_gnr" > Action</a>&nbsp;<span>|</span>
		  |<a href="/genre/Adventure?ref_=tt_stry_gnr" > Adventure</a>&nbsp;<span>|</span>
		  |<a href="/genre/Comedy?ref_=tt_stry_gnr" > Comedy</a>&nbsp;<span>|</span>
		  |<a href="/genre/Family?ref_=tt_stry_gnr" > Family</a>
		  |        </div>      
		  |        
		  |             <hr/>
		  |    
		  |    <div class="txt-block">
		  |                <h4>Motion Picture Rating
		  |                    (<a href="/mpaa?ref_=tt_stry_pg" >MPAA</a>)
		  |                </h4>
		  |            <span itemprop="contentRating">Rated PG for sequences of martial arts action</span>
		  |<span class="ghost">|</span>            <span class="see-more inline">
		  |<a href="/title/tt0441773/parentalguide?ref_=tt_stry_pg#certification" > See all certifications</a>&nbsp;&raquo;
		  |            </span>
		  |    </div>
		  |    <div class="txt-block">
		  |        <h4 class="inline">Parents Guide:</h4>
		  |        <span class="see-more inline" itemprop="audience" itemscope itemtype="http://schema.org/Audience">
		  |<a href="/title/tt0441773/parentalguide?ref_=tt_stry_pg" itemprop='url'> View content advisory</a>&nbsp;&raquo;
		  |        </span>
		  |    </div>
		  |    </div>
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_TitleStorylineWidget_finished');
		  |    }
		  |  </script>
		  |            
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_TitleDetailsWidget_started');
		  |    }
		  |  </script>
		  |    
		  |    <div class = "article" id="titleDetails">
		  |    <span class=rightcornerlink >
		  |            <a href="/register/login?why=edit&ref_=tt_dt_dt" rel="login">Edit</a>
		  |    </span>
		  |        <h2>Details</h2>
		  |      <div class="txt-block">
		  |      <h4 class="inline">Official Sites:</h4>
		  |        <a rel="nofollow" href="/offsite/?page-action=offsite-kungfupanda&token=BCYvPv0zezBefD1VZ4iZyG4nsiTT69623J-6AD6e7Vc44ds8XNFMB9biru-s1ezM7I3WNUz_zo98%0D%0A76Q7LbRKrC3nZof8w5Pddc7xL70hWHgJ2RtRckH2qxuFx25O8mV_WrRlu414XmBU7npKGVyO8WRE%0D%0ApnGPpuMOLg2W5IvaLqJ_o-XMd1y4GVEdZ99HILDViW-FOE9rcjyszx26YsMl3lpFrQ%0D%0A" itemprop='url'>DreamWorks [United States]</a>
		  |          <span class="ghost">|</span>
		  |        
		  |        <a rel="nofollow" href="/offsite/?page-action=offsite-facebook&token=BCYopKr_GsPdznG9Xjxl0MqjnB99ugs9WljPaO7-binRJm-hPA6F221zGYss9ZcT511m4B7wD16x%0D%0AxcZ9W3aJqc1ur4ffyVZbXtDM_8T7H-X5LDvUJ0tesahDqa656tmfC5x-sPqoPYX_QsWye_mS2042%0D%0A_5BiUxw75IF9OG1OIz0cATSdx7pR-1iN6YkDCvQtGuMIH-B8GU0psuWmGTEMjHm2ct55xiCF4jwY%0D%0AxL11sXE0rxo%0D%0A" itemprop='url'>Official Facebook</a>
		  |          <span class="ghost">|</span>
		  |               <span class="see-more inline">
		  |          <a href="externalsites?ref_=tt_dt_dt#official" itemprop='url'>See more</a>&nbsp;&raquo;
		  |      </span>
		  |      </div>
		  |  
		  |    <div class="txt-block">
		  |    <h4 class="inline">Country:</h4>
		  |        <a href="/country/us?ref_=tt_dt_dt" itemprop='url'>USA</a>
		  |    </div>
		  |  
		  |    <div class="txt-block">
		  |    <h4 class="inline">Language:</h4>
		  |        <a href="/language/en?ref_=tt_dt_dt" itemprop='url'>English</a>
		  |    </div>
		  |  
		  |    
		  |    <div class="txt-block">
		  |    <h4 class="inline">Release Date:</h4> 6 June 2008 (USA)
		  |    <span class="see-more inline">
		  |      <a href="releaseinfo?ref_=tt_dt_dt" itemprop='url'>See more</a>&nbsp;&raquo;
		  |    </span>
		  |    </div>
		  |  
		  |      <div class="txt-block">
		  |      <h4 class="inline">Also Known As:</h4> Kung Fu Panda: The IMAX Experience
		  |      <span class="see-more inline">
		  |        <a href="releaseinfo?ref_=tt_dt_dt#akas" itemprop='url'>See more</a>&nbsp;&raquo;
		  |      </span>
		  |      </div>
		  |  
		  |  
		  |    <hr /> 
		  |    <h3>Box Office</h3>
		  |  
		  |      <div class="txt-block">
		  |      <h4 class="inline">Budget:</h4>        $130,000,000        
		  |
		  |      <span class="attribute">(estimated)</span>
		  |      </div>
		  |  
		  |    <div class="txt-block">
		  |    <h4 class="inline">Opening Weekend:</h4>         &pound;6,069,679        
		  |
		  |      (UK)
		  |      <span class="attribute">(4 July 2008)</span>
		  |    </div>
		  |  
		  |    <div class="txt-block">
		  |    <h4 class="inline">Gross:</h4>        $215,395,021        
		  |
		  |      <span class="attribute">(USA)</span>
		  |      <span class="attribute">(3 October 2008)</span>
		  |    </div>
		  |  
		  |  <span class="see-more inline">
		  |    <a href="business?ref_=tt_dt_bus" itemprop='url'>See more</a>&nbsp;&raquo;
		  |  </span>  
		  |  <hr /> 
		  |  <h3>Company Credits</h3>
		  |    <div class="txt-block">
		  |    <h4 class="inline">Production Co:</h4>
		  |        <span itemprop="creator" itemscope itemtype="http://schema.org/Organization">
		  |<a href="/company/co0129164?ref_=tt_dt_co" itemprop='url'><span class="itemprop" itemprop="name">DreamWorks Animation</span></a></span>,        <span itemprop="creator" itemscope itemtype="http://schema.org/Organization">
		  |<a href="/company/co0069464?ref_=tt_dt_co" itemprop='url'><span class="itemprop" itemprop="name">Pacific Data Images (PDI)</span></a></span>      <span class="see-more inline">
		  |        <a href="companycredits?ref_=tt_dt_co" itemprop='url'>See more</a>&nbsp;&raquo;
		  |      </span>
		  |    </div>
		  |  <div class="txt-block"> 
		  |  Show detailed
		  |<a href="http://pro.imdb.com/title/tt0441773/companycredits?ref_=cons_tt_cocred_tt" itemprop='url'>company contact information</a>
		  |  on 
		  |  <a href="http://pro.imdb.com/?ref_=cons_tt_cocred_spl" itemprop='url'>IMDbPro</a>&nbsp;&raquo;
		  |  </div>
		  |  
		  |
		  |  <hr />
		  |  <h3>Technical Specs</h3>
		  |  
		  |    <div class="txt-block">
		  |      <h4 class="inline">Runtime:</h4> 
		  |        <time itemprop="duration" datetime="PT90M">90 min</time>
		  |    </div>
		  |  
		  |    <div class="txt-block">
		  |    <h4 class="inline">Sound Mix:</h4>
		  |        <a href="/search/title?sound_mixes=sonics_ddp&ref_=tt_dt_spec" itemprop='url'>Sonics-DDP</a>
		  |(IMAX version)<span class="ghost">|</span>        <a href="/search/title?sound_mixes=sdds&ref_=tt_dt_spec" itemprop='url'>SDDS</a>
		  |<span class="ghost">|</span>        <a href="/search/title?sound_mixes=dolby_digital&ref_=tt_dt_spec" itemprop='url'>Dolby Digital</a>
		  |<span class="ghost">|</span>        <a href="/search/title?sound_mixes=dts&ref_=tt_dt_spec" itemprop='url'>DTS</a>
		  |    </div>
		  |  
		  |    <div class="txt-block">
		  |    <h4 class="inline">Color:</h4>
		  |        <a href="/search/title?colors=color&ref_=tt_dt_spec" itemprop='url'>Color</a>
		  |    </div>
		  |  
		  |    <div class="txt-block">
		  |    <h4 class="inline">Aspect Ratio:</h4> 2.35 : 1
		  |    </div>
		  |  
		  |  See <a href="technical?ref_=tt_dt_spec" itemprop='url'>full technical specs</a>&nbsp;&raquo;
		  |    </div>
		  |
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_TitleDetailsWidget_finished');
		  |    }
		  |  </script>
		  |            
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_TitleDidYouKnowWidget_started');
		  |    }
		  |  </script>
		  |    <div id="titleDidYouKnow" class="article">
		  |    <span class=rightcornerlink >
		  |            <a href="/register/login?why=edit&ref_=tt_trv_trv" rel="login">Edit</a>
		  |    </span>
		  |        <h2>Did You Know?</h2>
		  |    <div id="trivia" class="txt-block">
		  |        <h4>Trivia</h4>
		  |
		  |
		  |Oogway's Chinese name, as shown in the end credits, means "Tortoise" ("Wu-gui" in Pinyin transliteration). Oogway is a tortoise, and often wears a cassock with the markings of a stylized tai-ji/ tai-chi fish diagram on his back.        <a href="trivia?ref_=tt_trv_trv" class="nobr" >See more</a>  &raquo;
		  |    </div>
		  |                <hr />
		  |     <div id="goofs"  class="txt-block">
		  |        <h4>Goofs</h4>
		  |During Tai-Lung's escape scene, he tosses 4 spears into the air. When he jumps up he only kicks 3 spears, but 4 are shown crashing into the prison wall.        <a href="trivia?tab=gf&ref_=tt_trv_gf" class="nobr" >See more</a>  &raquo;
		  |    </div>   
		  |                <hr />
		  |    <div id="quotes" class="text-block">
		  |        <h4>Quotes</h4>
		  |[<span class="fine">first lines</span>]
		  |<br /><a href="/name/nm0085312/?ref_=tt_trv_qu" ><span class="character">Po</span></a>:
		  |Legend tells of a legendary warrior whose kung fu skills were the stuff of legend.
		  |<br />        <a href="trivia?tab=qt&ref_=tt_trv_qu" class="nobr" >See more</a> &raquo;
		  |    </div>
		  |                <hr />
		  |     <div id="crazyCredits"  class="txt-block">
		  |        <h4>Crazy Credits</h4>
		  |During the closing credits, all the key characters (with their voice-actors' names) are seen interacting with the panda training doll. Afterwards, a montage appears, of pictures showing the main characters after the film's events.        <a href="trivia?tab=cz&ref_=tt_trv_cc" class="nobr" >See more</a>  &raquo;
		  |    </div>   
		  |                <hr />
		  |    <div id="connections" class="text-block">
		  |        <h4>Connections</h4>
		  |        Referenced in <a href="/title/tt3258322/?ref_=tt_trv_cnn">Troldspejlet: Episode #41.10</a>&nbsp;(2009)
		  |
		  |
		  |        <a href="trivia?tab=mc&ref_=tt_trv_cnn" class="nobr" >See more</a> &raquo;
		  |    </div>
		  |                <hr />
		  |    <div id="soundtracks" class="text-block">
		  |        <h4>Soundtracks</h4>
		  |Kung Fu Fighting<br />
		  |Written by <a href="/name/nm1222432/?ref_=tt_trv_snd">Carl Douglas</a><br />
		  |Performed by <a href="/name/nm0123741/?ref_=tt_trv_snd">CeeLo Green</a> (as Cee-Lo Green) and <a href="/name/nm0085312/?ref_=tt_trv_snd">Jack Black</a><br />
		  |Produced by <a href="/name/nm1561141/?ref_=tt_trv_snd">The Underdogs</a><br />
		  |Vocals produced by <a href="/name/nm0123741/?ref_=tt_trv_snd">CeeLo Green</a> (as Cee-Lo Green)<br />
		  |Cee-Lo Green appears courtesy of Radiculture Records/Downtown Recordings/Atlantic Recording Corp.<br />
		  |Jack Black appears courtesy of Epic Records<br />        <a href="soundtrack?ref_=tt_trv_snd" class="nobr" >See more</a> &raquo;
		  |    </div>
		  |    </div>
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_TitleDidYouKnowWidget_finished');
		  |    }
		  |  </script>
		  |    </div>
		  |</div> 
		  |
		  |<div id="content-1" class="redesign clear">
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_TitleFAQWidget_started');
		  |    }
		  |  </script>
		  |    
		  |    <div class="article" id="titleFAQ">
		  |        <h2>Frequently Asked Questions</h2>
		  |        
		  |            <div class="faq" >
		  |                    <div class="odd">
		  |                    <b>Q:</b>
		  |<a href="/title/tt0441773/faq?ref_=tt_faq_1#.2.1.2" > Is there a scene after the credits?</a>
		  |                    </div>
		  |                    <div class="even">
		  |                    <b>Q:</b>
		  |<a href="/title/tt0441773/faq?ref_=tt_faq_2#.2.1.14" > Do Kung Fu Masters teach different styles?</a>
		  |                    </div>
		  |                    <div class="odd">
		  |                    <b>Q:</b>
		  |<a href="/title/tt0441773/faq?ref_=tt_faq_3#.2.1.10" > How much sex, violence, and profanity are in this movie?</a>
		  |                    </div>
		  |            </div>
		  |        
		  |            <span class="see-more inline" >        
		  |                <a href="/title/tt0441773/faq?ref_=tt_faq_sm" class="nobr" >See more</a>
		  |                <span class="spoilers">(Spoiler Alert!)</span></span>&nbsp;&raquo;
		  |    </div>
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_TitleFAQWidget_finished');
		  |    }
		  |  </script>
		  |    
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_TitleUserReviewsWidget_started');
		  |    }
		  |  </script>
		  |    <div class="article" id="titleUserReviewsTeaser">
		  |        <h2>User Reviews</h2>
		  |        <div class="user-comments">
		  |                    <div class="tinystarbar" title="9/10">
		  |                        <div style="width: 90px;">&nbsp;</div>
		  |                    </div>
		  |                <span itemprop="review" itemscope itemtype="http://schema.org/Review">  
		  |                    <strong itemprop="name">Kick ass movie, almost note perfect</strong>
		  |                    <span itemprop="reviewRating" itemscope itemtype="http://schema.org/Rating">
		  |                        <meta itemprop="worstRating" content = "1" />
		  |                        <meta itemprop="ratingValue" content="9" />
		  |                        <meta itemprop="bestRating" content="10" />
		  |                    </span>
		  |                    <div class="comment-meta">
		  |                        28 June 2008 | by <a href="/user/ur19308657/?ref_=tt_urv" ><span itemprop="author">Brendan King</span></a>
		  |                        <meta itemprop="datePublished" content="2008-06-28" />
		  |                              (New Zealand)
		  |                        &ndash; <a href="/user/ur19308657/comments?ref_=tt_urv" >See all my reviews</a>
		  |                    </div>
		  |                    <div>
		  |                        <p itemprop="reviewBody">I saw this movie in the Cinema last night. I can not recommend this movie highly enough to kids of all ages. it is a long, long time since i have heard a Cinema audience laughing so much during a movie ( i think the last time was during Gigli!).<br /><br />Finally Dreamworks have managed to surpass the achievements of Pixar. All the elements are beyond anything i have seen in an Animated movie since The Lion King. the animation is hugely detailed and achingly beautiful, the script is the best i have come across in years. the only bum note in this symphony of fun is the fact that some of the immensely talented voice cast are badly underused. i can only remember hearing Jackie Chan&#39;s voice 3 times at most. having said that, the movie rests on the shoulders of jack Black and Dustin Hoffman. while playing their parts in a very different manner to each other, both are note perfect. they invest their characters with an energy (Black) and dignity (hoffman) that i could not find any fault with, even if i wanted to, not that i want to.<br /><br />Lastly, i believe that Oogway is the best animated character ever rendered, his facial expressions are hilarious and-surpass any previous attempts to give a character convincing facial expressions (i consider Gollum to be a CG character, not an animated character, for the record).<br /><br />in short, this is an utterly fantastic movie that everyone should watch 9/10</p>
		  |                    </div>
		  |                </span>
		  |                <hr />
		  |                <div class="yn" id="ynd_1901059">
		  |                    73 of 85 people found this review helpful.&nbsp;
		  |                    Was this review helpful to you?
		  |                    <button class="btn small" value="Yes" name="ynb_1901059_yes" onclick="CS.TMD.user_review_vote(1901059, 'tt0441773', 'yes');" >Yes</button>
		  |                    <button class="btn small" value="No" name="ynb_1901059_no" onclick="CS.TMD.user_review_vote(1901059, 'tt0441773', 'no');" >No</button>
		  |                </div>
		  |            <div class="see-more">
		  |                
		  |                <a href="/title/tt0441773/reviews-enter?ref_=tt_urv" rel="login">Review this title</a>
		  |                <span>|</span>
		  |                    <a href="/title/tt0441773/reviews?ref_=tt_urv" >See all 336 user reviews</a>&nbsp;&raquo;
		  |            </div>
		  |        </div>
		  |    </div>
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_TitleUserReviewsWidget_finished');
		  |    }
		  |  </script>
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_TitleUserReviewsWidget_finished');
		  |    }
		  |  </script>
		  |    
		  |    <div class="article" id="boardsTeaser">
		  |        <h2>Message Boards</h2>
		  |            Recent Posts
		  |            <table class="boards">
		  |                    <tr class="odd">
		  |                        <td>
		  |                            <a href="/title/tt0441773/board/nest/168738346?ref_=tt_bd_1" >ending a little disappointing</a>
		  |                        </td>
		  |                        <td>
		  |                            <a href="/user/ur22544916/?ref_=tt_bd_1" >god-king-kalu</a>
		  |                        </td>
		  |                    </tr>
		  |                    <tr class="even">
		  |                        <td>
		  |                            <a href="/title/tt0441773/board/nest/126287851?ref_=tt_bd_2" >Favorite Character?</a>
		  |                        </td>
		  |                        <td>
		  |                            <a href="/user/ur16181237/?ref_=tt_bd_2" >breakaway385</a>
		  |                        </td>
		  |                    </tr>
		  |                    <tr class="odd">
		  |                        <td>
		  |                            <a href="/title/tt0441773/board/nest/150776192?ref_=tt_bd_3" >this deserves a lot more than it did...</a>
		  |                        </td>
		  |                        <td>
		  |                            <a href="/user/ur5187977/?ref_=tt_bd_3" >Mightybull</a>
		  |                        </td>
		  |                    </tr>
		  |                    <tr class="even">
		  |                        <td>
		  |                            <a href="/title/tt0441773/board/nest/158605099?ref_=tt_bd_4" >Not many favorable reviews</a>
		  |                        </td>
		  |                        <td>
		  |                            <a href="/user/ur2273428/?ref_=tt_bd_4" >childers-3</a>
		  |                        </td>
		  |                    </tr>
		  |                    <tr class="odd">
		  |                        <td>
		  |                            <a href="/title/tt0441773/board/nest/225253032?ref_=tt_bd_5" >The animation looks beautiful</a>
		  |                        </td>
		  |                        <td>
		  |                            <a href="/user/ur20511172/?ref_=tt_bd_5" >head1993</a>
		  |                        </td>
		  |                    </tr>
		  |            </table>
		  |        <div class="see-more">
		  |            <a href="/title/tt0441773/board/?ref_=tt_bd_sm" >Discuss Kung Fu Panda (2008)</a> on the IMDb message boards &raquo;
		  |        </div>
		  |    </div>
		  |    
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_TitleContributeWidget_started');
		  |    }
		  |  </script>
		  |
		  |    <div class="article contribute">
		  |        <div class="rightcornerlink">
		  |<a href="/help/?adding/&ref_=tt_cn_hlp" >Getting Started</a>
		  |            <span>|</span>
		  |<a href="/czone/?ref_=tt_cn_cz" >Contributor Zone</a>&nbsp;&raquo;</div>
		  |        <h2>Contribute to This Page</h2>
		  |
		  |            <div class="button-box">
		  |                <form method="post" action="/updates?ref_=tt_cn_edt">
		  |                    <input type="hidden" name="auto" value="legacy/title/tt0441773/">
		  |                        <button class="btn primary large" rel="login" type="submit">Edit page</button>
		  |                </form>
		  |            </div>
		  |        
		  |
		  |    
		  |
		  |        <div class="button-box">
		  |<a href="/title/tt0441773/reviews-enter?ref_=tt_cn_urv" class="btn large" rel="login">Write review</a>          
		  |        </div>
		  |    
		  |    
		  |    
		  |    </div>
		  |
		  |  <script>
		  |    if ('csm' in window) {
		  |      csm.measure('csm_TitleContributeWidget_finished');
		  |    }
		  |  </script>
		  |  
		  |</div>
		  |
		  |                   <br class="clear" />
		  |                </div>
		  |
		  |
		  |  <div id="footer" class="ft">
		  |    <hr width="100%" size=1>
		  |    <div id="rvi-div">
		  |        <div class="recently-viewed">&nbsp;</div>
		  |        <br class="clear">
		  |        <hr width="100%" size="1">
		  |    </div>
		  |	
		  |	<!-- begin BOTTOM_AD -->
		  |<div id="bottom_ad_wrapper" class="dfp_slot">
		  |<script type="text/javascript">
		  |doWithAds(function(){
		  |ad_utils.register_ad('bottom_ad');
		  |});
		  |</script>
		  |<iframe data-dart-params="#imdb2.consumer.title/maindetails;!TILE!;sz=728x90,2x1;p=b;g=fm;g=co;g=an;g=ac;g=ad;g=baa;tt=f;m=PG;mh=PG;ml=PG;coo=us;fv=1;id=tt0441773;ab=c;bpx=2;md=tt0441773;s=3075;s=32;s=1009;s=3717;oe=utf-8;[CLIENT_SIDE_KEYVALUES];u=065068742621;ord=065068742621?" id="bottom_ad" name="bottom_ad" class="yesScript" width="728" height="90" data-original-width="728" data-original-height="90" data-config-width="728" data-config-height="90" data-cookie-width="null" data-cookie-height="null" marginwidth="0" marginheight="0" frameborder="0" scrolling="no" allowtransparency="true" onload="doWithAds.call(this, function(){ ad_utils.on_ad_load(this); });"></iframe>
		  |<noscript><a href="http://ad.doubleclick.net/N4215/jump/imdb2.consumer.title/maindetails;tile=3;sz=728x90,2x1;p=b;g=fm;g=co;g=an;g=ac;g=ad;g=baa;tt=f;m=PG;mh=PG;ml=PG;coo=us;fv=1;id=tt0441773;ab=c;bpx=2;md=tt0441773;s=3075;s=32;s=1009;s=3717;ord=065068742621?" target="_blank"><img src="http://ad.doubleclick.net/N4215/ad/imdb2.consumer.title/maindetails;tile=3;sz=728x90,2x1;p=b;g=fm;g=co;g=an;g=ac;g=ad;g=baa;tt=f;m=PG;mh=PG;ml=PG;coo=us;fv=1;id=tt0441773;ab=c;bpx=2;md=tt0441773;s=3075;s=32;s=1009;s=3717;ord=065068742621?" border="0" alt="advertisement" /></a></noscript>
		  |</div>
		  |<div id="bottom_ad_reflow_helper"></div>
		  |<script>
		  |doWithAds(function(){
		  |ad_utils.gpt.render_ad('bottom_ad');
		  |}, "ad_utils not defined, unable to render client-side GPT ad.");
		  |</script>
		  |<!-- End BOTTOM_AD -->
		  |	
		  |    <p class="footer" align="center">
		  |    
		  |        <a
		  |onclick="(new Image()).src='/rg/home/footer/images/b.gif?link=/';"
		  |href="/"
		  |>Home</a>
		  |        | <a
		  |onclick="(new Image()).src='/rg/search/footer/images/b.gif?link=/search';"
		  |href="/search"
		  |>Search</a>
		  |        | <a
		  |onclick="(new Image()).src='/rg/siteindex/footer/images/b.gif?link=/a2z';"
		  |href="/a2z"
		  |>Site Index</a>
		  |        | <a
		  |onclick="(new Image()).src='/rg/intheaters/footer/images/b.gif?link=/movies-in-theaters/';"
		  |href="/movies-in-theaters/"
		  |>In Theaters</a>
		  |        | <a
		  |onclick="(new Image()).src='/rg/comingsoon/footer/images/b.gif?link=/movies-coming-soon/';"
		  |href="/movies-coming-soon/"
		  |>Coming Soon</a>
		  |        | <a
		  |onclick="(new Image()).src='/rg/topmovies/footer/images/b.gif?link=/chart/';"
		  |href="/chart/"
		  |>Top Movies</a>
		  |        | <a
		  |onclick="(new Image()).src='/rg/top250/footer/images/b.gif?link=/chart/top';"
		  |href="/chart/top"
		  |>Top 250</a>
		  |        | <a
		  |onclick="(new Image()).src='/rg/tv/footer/images/b.gif?link=/sections/tv/';"
		  |href="/sections/tv/"
		  |>TV</a>
		  |        | <a
		  |onclick="(new Image()).src='/rg/news/footer/images/b.gif?link=/news/';"
		  |href="/news/"
		  |>News</a>
		  |        | <a
		  |onclick="(new Image()).src='/rg/messageboards/footer/images/b.gif?link=/boards/';"
		  |href="/boards/"
		  |>Message Boards</a>
		  |        | <a
		  |onclick="(new Image()).src='/rg/pressroom/footer/images/b.gif?link=/pressroom/';"
		  |href="/pressroom/"
		  |>Press Room</a>
		  |        <br>
		  |
		  |        <a
		  |onclick="(new Image()).src='/rg/register-v2/footer/images/b.gif?link=https://secure.imdb.com/register-imdb/form-v2';"
		  |href="https://secure.imdb.com/register-imdb/form-v2"
		  |>Register</a>
		  |        | <a
		  |onclick="(new Image()).src='/rg/advertising/footer/images/b.gif?link=/advertising/';"
		  |href="/advertising/"
		  |>Advertising</a>
		  |        | <a
		  |onclick="(new Image()).src='/rg/helpdesk/footer/images/b.gif?link=/helpdesk/contact';"
		  |href="/helpdesk/contact"
		  |>Contact Us</a>
		  |        | <a
		  |onclick="(new Image()).src='/rg/jobs/footer/images/b.gif?link=/jobs';"
		  |href="/jobs"
		  |>Jobs</a>
		  |        | <a href="http://pro.imdb.com/?ref_=cons_ft_hm" >IMDbPro</a>
		  |        | <a
		  |onclick="(new Image()).src='/rg/BOXOFFICEMOJO/FOOTER/images/b.gif?link=http://www.boxofficemojo.com/';"
		  |href="http://www.boxofficemojo.com/"
		  |>Box Office Mojo</a>
		  |        | <a
		  |onclick="(new Image()).src='/rg/WITHOUTABOX/FOOTER/images/b.gif?link=http://www.withoutabox.com/';"
		  |href="http://www.withoutabox.com/"
		  |>Withoutabox</a>
		  |        <br /><br />
		  |        IMDb Mobile:
		  |          <a
		  |onclick="(new Image()).src='/rg/mobile-ios/footer/images/b.gif?link=/apps/ios/';"
		  |href="/apps/ios/"
		  |>iPhone/iPad</a>
		  |        | <a
		  |onclick="(new Image()).src='/rg/mobile-android/footer/images/b.gif?link=/android';"
		  |href="/android"
		  |>Android</a>
		  |        | <a
		  |onclick="(new Image()).src='/rg/mobile-web/footer/images/b.gif?link=http://m.imdb.com';"
		  |href="http://m.imdb.com"
		  |>Mobile site</a>
		  |        | <a
		  |onclick="(new Image()).src='/rg/mobile-win7/footer/images/b.gif?link=/windowsphone';"
		  |href="/windowsphone"
		  |>Windows Phone 7</a>
		  |        | IMDb Social:
		  |          <a
		  |onclick="(new Image()).src='/rg/facebook/footer/images/b.gif?link=http://www.facebook.com/imdb';"
		  |href="http://www.facebook.com/imdb"
		  |>Facebook</a>
		  |        | <a
		  |onclick="(new Image()).src='/rg/twitter/footer/images/b.gif?link=http://twitter.com/imdb';"
		  |href="http://twitter.com/imdb"
		  |>Twitter</a>
		  |       <br /><br />
		  |    </p>
		  |  
		  |    <p class="footer" align="center">
		  |        <a
		  |onclick="(new Image()).src='/rg/help/footer/images/b.gif?link=/help/show_article?conditions';"
		  |href="/help/show_article?conditions"
		  |>Copyright &copy;</a> 1990-2014 
		  |        <a
		  |onclick="(new Image()).src='/rg/help/footer/images/b.gif?link=/help/';"
		  |href="/help/"
		  |>IMDb.com, Inc.</a>
		  |        <br>
		  |        <a
		  |onclick="(new Image()).src='/rg/help/footer/images/b.gif?link=/help/show_article?conditions';"
		  |href="/help/show_article?conditions"
		  |>Conditions of Use</a> | <a
		  |onclick="(new Image()).src='/rg/help/footer/images/b.gif?link=/privacy';"
		  |href="/privacy"
		  |>Privacy Policy</a> | <a
		  |onclick="(new Image()).src='/rg/help/footer/images/b.gif?link=//www.amazon.com/InterestBasedAds';"
		  |href="//www.amazon.com/InterestBasedAds"
		  |>Interest-Based Ads</a>
		  |        <br>
		  |        An <span id="amazon_logo" class="footer_logo" align="middle"></span> company.
		  |    </p>
		  |  
		  |
		  |      
		  |           
		  |           
		  |      
		  |  <table class="footer" id="amazon-affiliates">
		  |    <tr>
		  |      <td colspan="8">
		  |        Amazon Affiliates
		  |      </td>
		  |    </tr>
		  |    <tr>
		  |      <td class="amazon-affiliate-site-first">
		  |       
		  |        <a class="amazon-affiliate-site-link" href="http://www.amazon.de/b/ref=sa_menu_aiv?ie=UTF8&node=3010075031&tag=imdbpr1-20">
		  |          <span class="amazon-affiliate-site-name">Amazon Instant Video</span><br>
		  |          <span class="amazon-affiliate-site-desc">Watch Movies &<br>TV Online</span>
		  |        </a>
		  |      </td>
		  |     
		  |    <td class="amazon-affiliate-site-item-nth">
		  |        <a class="amazon-affiliate-site-link" href=http://www.amazon.de/b/?_encoding=UTF8&node=3279204031&tag=imdbpr1-20 >
		  |          <span class="amazon-affiliate-site-name">Prime Instant Video</span><br>
		  |          <span class="amazon-affiliate-site-desc">Unlimited Streaming<br>of Movies & TV</span>
		  |        </a>
		  |    </td>
		  |    <td class="amazon-affiliate-site-item-nth">
		  |        <a class="amazon-affiliate-site-link" href=http://www.amazon.de/b?ie=UTF8&node=284266&tag=imdbpr1-de-21 >
		  |          <span class="amazon-affiliate-site-name">Amazon Germany</span><br>
		  |          <span class="amazon-affiliate-site-desc">Buy Movies on<br>DVD & Blu-ray</span>
		  |        </a>
		  |    </td>
		  |    <td class="amazon-affiliate-site-item-nth">
		  |        <a class="amazon-affiliate-site-link" href=http://www.amazon.it/b?ie=UTF8&node=412606031&tag=imdbpr1-it-21 >
		  |          <span class="amazon-affiliate-site-name">Amazon Italy</span><br>
		  |          <span class="amazon-affiliate-site-desc">Buy Movies on<br>DVD & Blu-ray</span>
		  |        </a>
		  |    </td>
		  |    <td class="amazon-affiliate-site-item-nth">
		  |        <a class="amazon-affiliate-site-link" href=http://www.amazon.fr/b?ie=UTF8&node=405322&tag=imdbpr1-fr-21 >
		  |          <span class="amazon-affiliate-site-name">Amazon France</span><br>
		  |          <span class="amazon-affiliate-site-desc">Buy Movies on<br>DVD & Blu-ray</span>
		  |        </a>
		  |    </td>
		  |    <td class="amazon-affiliate-site-item-nth">
		  |        <a class="amazon-affiliate-site-link" href=http://www.amazon.in/movies-tv-shows/b/?ie=UTF&node=976416031&tag=imdbpr1-in-21 >
		  |          <span class="amazon-affiliate-site-name">Amazon India</span><br>
		  |          <span class="amazon-affiliate-site-desc">Buy Movie and<br>TV Show DVDs</span>
		  |        </a>
		  |    </td>
		  |    <td class="amazon-affiliate-site-item-nth">
		  |        <a class="amazon-affiliate-site-link" href=http://www.dpreview.com >
		  |          <span class="amazon-affiliate-site-name">DPReview</span><br>
		  |          <span class="amazon-affiliate-site-desc">Digital<br>Photography</span>
		  |        </a>
		  |    </td>
		  |    <td class="amazon-affiliate-site-item-nth">
		  |        <a class="amazon-affiliate-site-link" href=http://www.audible.com >
		  |          <span class="amazon-affiliate-site-name">Audible</span><br>
		  |          <span class="amazon-affiliate-site-desc">Download<br>Audio Books</span>
		  |        </a>
		  |    </td>
		  |    </tr>
		  |  </table>
		  |  </div>
		  |            </div>
		  |        </div>
		  |
		  |<script type="text/javascript" src="http://ia.media-imdb.com/images/G/01/imdb/js/collections/title-2128293095._V335858255_.js"></script>
		  |
		  |<script type="text/imdblogin-js" id="login">
		  |jQuery(document).ready(function(){
		  |    window.imdb.login_lightbox("https://secure.imdb.com", "http://www.imdb.com/title/tt0441773/");
		  |});
		  |</script>
		  |
		  |        <script type="text/javascript">
		  |                jQuery(
		  |                             function() {
		  |           var isAdvertisingThemed = !!(window.custom && window.custom.full_page && window.custom.full_page.theme),
		  |               url = "http://www.facebook.com/widgets/like.php?width=280&show_faces=1&layout=standard&href=http%3A%2F%2Fwww.imdb.com%2Ftitle%2Ftt0441773%2F&colorscheme=light",
		  |               like = document.getElementById('iframe_like');
		  |
		  |           if (!isAdvertisingThemed && like) {
		  |              like.src = url;
		  |              like.onload = function () { generic.monitoring.stop_timing('facebook_like_iframe', '', false); };
		  |           } else if (isAdvertisingThemed) {
		  |              $('.social_networking_like').closest('.aux-content-widget-2').hide();
		  |           }
		  |        }
		  |
		  |                );
		  |                jQuery(
		  |                             function() {
		  |            var facebookTheme = (window.custom && window.custom.full_page &&
		  |                    window.custom.full_page.theme) ?
		  |                window.custom.full_page.theme : "light",
		  |            url = "//www.facebook.com/plugins/likebox.php?href=facebook.com%2Fimdb&width=300&height=190&connections=4&header=false&stream=false&colorscheme=" + facebookTheme,
		  |            like = document.getElementById('facebookIframe'),
		  |            twitterIframe = document.getElementById('twitterIframe');
		  |            if (like) {
		  |                like.src = url;
		  |            }
		  |            if (twitterIframe) {
		  |                twitterIframe.src = "http://i.media-imdb.com/images/social/twitter.html?10#imdb";
		  |            }
		  |         }
		  |
		  |                );
		  |        </script>
		  |<!-- begin ads footer -->
		  |
		  |<!-- Begin SIS code --> 
		  |<iframe id="sis_pixel_sitewide" width="1" height="1" frameborder="0" marginwidth="0" marginheight="0" style="display: none;"></iframe>
		  |<script>
		  |    setTimeout(function(){
		  |        try{
		  |            //sis3.0 pixel
		  |            var url_sis3 = 'http://s.amazon-adsystem.com/iu3?',
		  |                params_sis3 = [
		  |                    "d=imdb.com",
		  |                    "a1=",
		  |                    "a2=0101a0e4bc937a9970a4271a6ecbe2f567cb3587f86d0185d18064bac771dbdfd3d8",
		  |                    "pId=tt0441773",
		  |                    "r=1",
		  |                    "rP=http%3A%2F%2Fwww.imdb.com%2Ftitle%2Ftt0441773%2F",
		  |                    "ex-hargs=v=1.0;c=IMDB;p=tt0441773;t=imdb_title_view;",
		  |                    "encoding=server",
		  |                    "cb=065068742621"  
		  |                ];
		  |        
		  |            (document.getElementById('sis_pixel_sitewide')).src = url_sis3 + params_sis3.join('&');
		  |        }
		  |        catch(e){
		  |            if ('consoleLog' in window){
		  |                consoleLog('Pixel failure ' + e.toString(),'sis');
		  |            }
		  |            if (window.ueLogError) { 
		  |                window.ueLogError(e);
		  |            }
		  |        }
		  |    }, 5);
		  |</script>
		  |<!-- End SIS code -->
		  |
		  |<!-- begin comscore beacon -->
		  |<script type="text/javascript" src='http://ia.media-imdb.com/images/G/01/imdbads/js/beacon-58460835._V379390778_.js'></script>
		  |<script type="text/javascript">
		  |if(window.COMSCORE){
		  |COMSCORE.beacon({
		  |c1: 2,
		  |c2:"6034961",
		  |c3:"",
		  |c4:"http://www.imdb.com/title/tt0441773/",
		  |c5:"",
		  |c6:"",
		  |c15:""
		  |});
		  |}
		  |</script>
		  |<noscript>
		  |<img src="http://b.scorecardresearch.com/p?c1=2&c2=6034961&c3=&c4=http%3A%2F%2Fwww.imdb.com%2Ftitle%2Ftt0441773%2F&c5=c6=&15=&cj=1"/>
		  |</noscript>
		  |<!-- end comscore beacon -->
		  |
		  |<script>
		  |    doWithAds(function(){
		  |        (new Image()).src = "http://www.amazon.com/aan/2009-05-01/imdb/default?slot=sitewide-iframe&u=065068742621&ord=065068742621";
		  |    },"unable to request AAN pixel");
		  |</script>
		  |
		  |<script>
		  |    doWithAds(function(){
		  |           window.jQuery && jQuery(function(){
		  |              generic.document_is_ready()
		  |           });
		  |           generic.monitoring.stop_timing('page_load','',true);
		  |           generic.monitoring.all_events_started();
		  |       }, "No monitoring or document_is_ready object in generic");
		  |</script>
		  |<!-- end ads footer -->
		  |
		  |<div id="servertime" time="188"/>
		  |
		  |
		  |
		  |<script>
		  |    if (typeof uet == 'function') {
		  |      uet("be");
		  |    }
		  |</script>
		  |    </body>
		  |</html>
		  |
		  |			|
		""".stripMargin
}
