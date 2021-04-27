package com.github.davidmoten.odata.client;

public final class Properties {

    private Properties() {
        // prevent instantiation
    }

    /**
     * Bug fix for Microsoft Graph only? When a collection is returned the editLink
     * is terminated with the subclass if the collection type has subclasses. For
     * example when a collection of Attachment (with full metadata) is requested the
     * editLink of an individual attachment may end in /itemAttachment to indicate
     * the type of the attachment. To get the $value download working we need to
     * remove that type cast and that type cast is removed if the value of this
     * property is set to "true".
     */
    public static final String MODIFY_STREAM_EDIT_LINK = "modify.stream.edit.link";

    /**
     * When calling an action or a function the odata specification suggests that
     * the full namespaced action/function name be added to the url path. Microsoft
     * Graph wasn't supporting this mid-2020 so this option was added. When this
     * property is set to true only the simple name of the action/function is
     * appended to the url path.
     */
    public static final String ACTION_OR_FUNCTION_SEGMENT_SIMPLE_NAME = "action.or.function.segment.simple.name";

    /**
     * Returning an {@code @odata.editLink} is the indicator that a stream exists
     * for an entity but Microsoft Graph can incorrectly return no value of
     * {@code @odata.editLink}. If this property is set to "true" then calling
     * {@code get} will never assume the stream does not exist even though no edit
     * link was returned and the edit link used will be the current path.
     */
    public static final String ATTEMPT_STREAM_WHEN_NO_METADATA = "attempt.stream.when.no.metadata";

    
    /**
     * When a create action occurrs the OData specified response code is 201 (
     * Created). If this property is set to "true" then any response code between
     * 200 and 299 will be accepted.
     */
    public static final String CREATE_RETURNS_ANY_HTTP_OK_STATUS_CODE = "create.returns.any.http.ok.status.code";

}
