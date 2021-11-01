package itreact.tutorup.server.db;



/**
 * Class used to return the DB level implementations.
 */
public class DatabaseFactory {
    private static ConnectionManager connManager;
    private static UserDao userDao;
    private static OffersDao offerDao;
    private static RequestsDao requestsDao;
    private static ReviewDao reviewDao;
    private static NotificationsDao notificationsDao;
    private static InteractionsDao interactionsDao;
    private static SubjectsDao subjectsDao;
    private static GradesDao gradesDao;

    public synchronized static ConnectionManager getConnectionManager() {
        if (connManager == null) {
            connManager = new ConnectionManagerImpl();
        }
        return connManager;
    }

    public synchronized static UserDao getUserDao() {
        if (userDao == null) {
            userDao = new UserDaoImpl(getConnectionManager());
        }
        return userDao;
    }

    public synchronized static OffersDao getOfferDao() {
        if (offerDao == null) {
            offerDao = new OffersDaoImpl(getConnectionManager());
        }
        return offerDao;
    }

    public synchronized static RequestsDao getRequestsDao(){
        if(requestsDao == null){
            requestsDao = new RequestsDaoImpl(getConnectionManager());

        }
        return requestsDao;
    }

    public synchronized static ReviewDao getReviewDao(){
        if(reviewDao == null){
            reviewDao = new ReviewDaoImpl(getConnectionManager());
        }
        return reviewDao;
    }

    public synchronized static NotificationsDao getNotificationsDao(){
        if(notificationsDao == null){
            notificationsDao = new NotificationsDaoImpl(getConnectionManager());
        }
        return notificationsDao;
    }

    public synchronized static InteractionsDao getInteractionsDao(){
        if(interactionsDao == null){
            interactionsDao = new InteractionsDaoImpl(getConnectionManager());
        }
        return interactionsDao;
    }

    public synchronized static SubjectsDao getSubjectsDao(){
        if(subjectsDao == null){
            subjectsDao = new SubjectsDaoImpl(getConnectionManager());
        }
        return subjectsDao;
    }

    public synchronized static GradesDao getGradesDao(){
        if(gradesDao == null){
            gradesDao = new GradesDaoImpl(getConnectionManager());
        }
        return gradesDao;
    }
}
